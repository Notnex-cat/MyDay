package com.notnex.myday.data

import android.util.Log
import androidx.room.withTransaction
import com.notnex.myday.auth.AuthRepository
import com.notnex.myday.data.remote.CloudDataSource
import com.notnex.myday.data.remote.MyDayFirebaseDTO
import com.notnex.myday.data.remote.MyScheduleFirebaseDTO
import com.notnex.myday.data.remote.toDto
import com.notnex.myday.data.remote.toEntityOrNull
import com.notnex.myday.di.ApplicationScope
import com.notnex.myday.neuralnetwork.ScheduleItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Единая точка доступа к дневнику: Room — источник правды, Firestore получает копии
 * изменений (last-write-wins по lastUpdated). Обратный поток из облака делает SyncManager.
 */
@Singleton
class MyDayRepository @Inject constructor(
    private val db: MyDayDataBase,
    private val dayDao: MyDayDAO,
    private val scheduleDao: MyScheduleDAO,
    private val cloud: CloudDataSource,
    private val auth: AuthRepository,
    @ApplicationScope private val appScope: CoroutineScope
) {
    // Read-modify-write записи дня не должны перемешиваться между собой.
    private val writeMutex = Mutex()

    // Пользовательские записи доводим до конца, даже если экран (и его viewModelScope)
    // закрылся сразу после нажатия «Сохранить».
    private suspend fun <T> write(block: suspend () -> T): T =
        withContext(NonCancellable) { writeMutex.withLock { block() } }

    // Room будит запрос при любой записи в таблицу, даже если этот день не менялся.
    // distinctUntilChanged не пускает такие повторы до экрана.
    fun observeDay(date: LocalDate): Flow<MyDayEntity?> =
        dayDao.observe(date).distinctUntilChanged()

    fun observeSchedule(date: LocalDate): Flow<List<ScheduleEntity>> =
        scheduleDao.observeByDate(date).distinctUntilChanged()

    suspend fun getDay(date: LocalDate): MyDayEntity? = dayDao.getOnce(date)

    suspend fun getSchedule(date: LocalDate): List<ScheduleEntity> = scheduleDao.getByDate(date)

    // ---------- День ----------

    suspend fun saveNote(date: LocalDate, note: String) = updateDay(date) { copy(note = note) }

    suspend fun saveRating(date: LocalDate, score: Double) = updateDay(date) { copy(score = score) }

    suspend fun saveAiFeedback(date: LocalDate, feedback: String) =
        updateDay(date) { copy(aiFeedback = feedback) }

    /** Сохранение, которое должно дожить до закрытия экрана (например, отложенный ввод заметки). */
    fun saveNoteInBackground(date: LocalDate, note: String) {
        appScope.launch { saveNote(date, note) }
    }

    private suspend fun updateDay(date: LocalDate, change: MyDayEntity.() -> MyDayEntity) {
        val updated = write {
            val current = dayDao.getOnce(date) ?: MyDayEntity(date)
            current.change().copy(lastUpdated = System.currentTimeMillis())
                .also { dayDao.upsert(it) }
        }
        pushInBackground { uid -> cloud.setDay(uid, updated.toDto()) }
    }

    // ---------- Расписание ----------

    /**
     * Делает расписание дня ровно таким, как [items]: лишние пункты удаляет, новые добавляет,
     * изменённые обновляет. Заметки и оценки уцелевших пунктов не теряются, а пункты без
     * изменений не перезаписываются вовсе.
     *
     * Всё идёт одной транзакцией Room и одной пачкой в облако: день не может остаться
     * заменённым наполовину, если приложение закроют посередине.
     */
    suspend fun replaceDaySchedule(date: LocalDate, items: List<ScheduleItem>) {
        val now = System.currentTimeMillis()
        val (changed, removedIds) = write {
            db.withTransaction {
                val current = scheduleDao.getByDate(date).associateBy { it.id }
                val keepIds = items.mapTo(HashSet()) { it.id }
                val removed = current.keys.filter { it !in keepIds }

                val changed = items.mapNotNull { item ->
                    val existing = current[item.id]
                    when {
                        existing == null -> ScheduleEntity(
                            id = item.id,
                            scheduleDate = date,
                            time = item.time,
                            scheduleItem = item.task,
                            lastUpdated = now
                        )
                        existing.time != item.time || existing.scheduleItem != item.task ->
                            existing.copy(time = item.time, scheduleItem = item.task, lastUpdated = now)
                        else -> null
                    }
                }

                if (removed.isNotEmpty()) scheduleDao.deleteByIds(removed)
                if (changed.isNotEmpty()) {
                    ensureDays(listOf(date))
                    scheduleDao.upsertAll(changed)
                }
                changed to removed
            }
        }
        if (changed.isEmpty() && removedIds.isEmpty()) return
        pushInBackground { uid ->
            cloud.applyScheduleChanges(uid, changed.map { it.toDto() }, removedIds)
        }
    }

    suspend fun updateScheduleItem(id: String, title: String, time: LocalTime, note: String) {
        val updated = write {
            scheduleDao.getById(id)
                ?.copy(scheduleItem = title, time = time, note = note, lastUpdated = System.currentTimeMillis())
                ?.also { scheduleDao.upsert(it) }
        } ?: return
        pushInBackground { uid -> cloud.setSchedule(uid, updated.toDto()) }
    }

    suspend fun deleteScheduleItem(id: String) {
        write { scheduleDao.deleteByIds(listOf(id)) }
        pushInBackground { uid -> cloud.deleteSchedule(uid, id) }
    }

    // Родительская строка дня нужна из-за внешнего ключа. Это локальная заглушка с lastUpdated = 0:
    // она не затирает настоящий день из облака и не выгружается в Firestore.
    private suspend fun ensureDays(dates: Collection<LocalDate>) {
        dayDao.insertIfAbsent(dates.distinct().map { MyDayEntity(date = it, lastUpdated = 0L) })
    }

    // ---------- Синхронизация (используется SyncManager) ----------

    suspend fun getAllDays(): List<MyDayEntity> = dayDao.getAll()

    suspend fun getAllSchedule(): List<ScheduleEntity> = scheduleDao.getAll()

    // Пачка из облака сливается одной транзакцией: при первом входе это сотни дней, и запись
    // по одному будила бы экраны сотни раз. Локальные версии читаются одним запросом на пачку.
    suspend fun mergeRemoteDays(remote: List<MyDayFirebaseDTO>) {
        val entities = remote.mapNotNull { it.toEntityOrNull() }
        if (entities.isEmpty()) return
        writeMutex.withLock {
            db.withTransaction {
                val local = entities.map { it.date }
                    .chunked(SQL_CHUNK)
                    .flatMap { dayDao.getByDates(it) }
                    .associateBy { it.date }
                val newer = entities.filter { remoteDay ->
                    local[remoteDay.date]?.let { remoteDay.lastUpdated > it.lastUpdated } ?: true
                }
                if (newer.isNotEmpty()) dayDao.upsertAll(newer)
            }
        }
    }

    suspend fun mergeRemoteSchedule(remote: List<MyScheduleFirebaseDTO>) {
        val entities = remote.mapNotNull { it.toEntityOrNull() }
        if (entities.isEmpty()) return
        writeMutex.withLock {
            db.withTransaction {
                val local = entities.map { it.id }
                    .chunked(SQL_CHUNK)
                    .flatMap { scheduleDao.getByIds(it) }
                    .associateBy { it.id }
                val newer = entities.filter { remoteItem ->
                    local[remoteItem.id]?.let { remoteItem.lastUpdated > it.lastUpdated } ?: true
                }
                if (newer.isNotEmpty()) {
                    ensureDays(newer.map { it.scheduleDate })
                    scheduleDao.upsertAll(newer)
                }
            }
        }
    }

    suspend fun removeRemoteSchedule(ids: List<String>) {
        if (ids.isEmpty()) return
        writeMutex.withLock {
            db.withTransaction {
                ids.chunked(SQL_CHUNK).forEach { scheduleDao.deleteByIds(it) }
            }
        }
    }

    /** При выходе из аккаунта локальные данные не должны утечь в облако следующего пользователя. */
    suspend fun clearLocal() {
        writeMutex.withLock { withContext(Dispatchers.IO) { db.clearAllTables() } }
    }

    // ---------- Отправка в облако ----------

    // Firestore ставит записи в собственную оффлайн-очередь, а await() завершается только после
    // ответа сервера. Поэтому пишем в облако «в фоне» и не блокируем локальное сохранение.
    private fun pushInBackground(block: suspend (uid: String) -> Unit) {
        val uid = auth.currentUserId ?: return
        appScope.launch {
            try {
                block(uid)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Cloud push failed: ${e.message}")
            }
        }
    }

    private companion object {
        const val TAG = "MyDayRepository"

        // SQLite до Android 11 принимает не больше 999 параметров в одном запросе.
        const val SQL_CHUNK = 500
    }
}
