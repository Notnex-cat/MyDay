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

    fun observeDay(date: LocalDate): Flow<MyDayEntity?> = dayDao.observe(date)


    fun observeNotes(): Flow<List<MyDayEntity>> = dayDao.observeNotes()

    fun observeSchedule(date: LocalDate): Flow<List<ScheduleEntity>> = scheduleDao.observeByDate(date)

    suspend fun getScheduleItem(id: String): ScheduleEntity? = scheduleDao.getById(id)

    // ---------- День ----------

    suspend fun saveNote(date: LocalDate, note: String) = updateDay(date) { copy(note = note) }

    suspend fun saveRating(date: LocalDate, score: Double) = updateDay(date) { copy(score = score) }
    suspend fun saveMood(date: LocalDate, mood: Int) = updateDay(date) { copy(mood = mood) }

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

    /** Сохраняет пункты, сгенерированные нейросетью; заметки и оценки уже существующих пунктов не теряются. */
    suspend fun addScheduleItems(date: LocalDate, items: List<ScheduleItem>) {
        if (items.isEmpty()) return
        val now = System.currentTimeMillis()
        val saved = write {
            db.withTransaction {
                ensureDay(date)
                items.map { item ->
                    val existing = scheduleDao.getById(item.id)
                    (existing ?: ScheduleEntity(
                        id = item.id,
                        scheduleDate = date,
                        time = item.time,
                        scheduleItem = item.task
                    ))
                        .copy(time = item.time, scheduleItem = item.task, lastUpdated = now)
                        .also { scheduleDao.upsert(it) }
                }
            }
        }
        pushInBackground { uid -> saved.forEach { cloud.setSchedule(uid, it.toDto()) } }
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
        write { scheduleDao.deleteById(id) }
        pushInBackground { uid -> cloud.deleteSchedule(uid, id) }
    }

    // Родительская строка дня нужна из-за внешнего ключа. Это локальная заглушка с lastUpdated = 0:
    // она не затирает настоящий день из облака и не выгружается в Firestore.
    private suspend fun ensureDay(date: LocalDate) {
        if (dayDao.getOnce(date) == null) {
            dayDao.upsert(MyDayEntity(date = date, lastUpdated = 0L))
        }
    }

    // ---------- Синхронизация (используется SyncManager) ----------

    suspend fun getAllDays(): List<MyDayEntity> = dayDao.getAll()

    suspend fun getAllSchedule(): List<ScheduleEntity> = scheduleDao.getAll()

    suspend fun mergeRemoteDays(remote: List<MyDayFirebaseDTO>) {
        writeMutex.withLock {
            for (entity in remote.mapNotNull { it.toEntityOrNull() }) {
                val local = dayDao.getOnce(entity.date)
                if (local == null || entity.lastUpdated > local.lastUpdated) dayDao.upsert(entity)
            }
        }
    }

    suspend fun mergeRemoteSchedule(remote: List<MyScheduleFirebaseDTO>) {
        writeMutex.withLock {
            db.withTransaction {
                for (entity in remote.mapNotNull { it.toEntityOrNull() }) {
                    val local = scheduleDao.getById(entity.id)
                    if (local == null || entity.lastUpdated > local.lastUpdated) {
                        ensureDay(entity.scheduleDate)
                        scheduleDao.upsert(entity)
                    }
                }
            }
        }
    }

    suspend fun removeRemoteSchedule(ids: List<String>) {
        writeMutex.withLock { ids.forEach { scheduleDao.deleteById(it) } }
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
    }
}
