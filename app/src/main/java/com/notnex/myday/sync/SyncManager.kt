package com.notnex.myday.sync

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.notnex.myday.auth.AuthRepository
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.data.remote.CloudDataSource
import com.notnex.myday.data.remote.toDto
import com.notnex.myday.di.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Жизненный цикл синхронизации, привязанный к аккаунту (а не к какому-либо экрану):
 *  - вход: подписываемся на облако, слияние по lastUpdated в обе стороны;
 *  - выход или смена аккаунта: чистим локальную БД, чтобы данные не перетекли к другому пользователю.
 *
 * Слушатели облака работают, только пока приложение на экране. Процесс будят и будильники
 * напоминаний, и ради одного уведомления держать соединение с Firestore незачем. Собственные
 * записи от этого не страдают: их отправляет MyDayRepository через оффлайн-очередь Firestore.
 */
@Singleton
class SyncManager @Inject constructor(
    private val auth: AuthRepository,
    private val repository: MyDayRepository,
    private val cloud: CloudDataSource,
    @ApplicationScope private val scope: CoroutineScope
) {
    fun start() {
        scope.launch {
            var previousUid: String? = null
            val uid = auth.authState.map { it.user?.userId }.distinctUntilChanged()
            combine(uid, appInForeground()) { currentUid, inForeground -> currentUid to inForeground }
                .distinctUntilChanged()
                // collectLatest сначала отменяет прежнюю синхронизацию и только потом запускает блок:
                // база чистится, когда слушатели старого аккаунта уже сняты.
                .collectLatest { (currentUid, inForeground) ->
                    // Очистка не зависит от видимости: выйти из аккаунта можно и в фоне.
                    if (previousUid != null && previousUid != currentUid) repository.clearLocal()
                    previousUid = currentUid
                    if (currentUid != null && inForeground) runSync(currentUid)
                }
        }
    }

    // true, пока на экране хотя бы одна активити. ProcessLifecycleOwner сообщает об уходе в фон
    // с задержкой 700 мс, поэтому поворот экрана и переход между активити синхронизацию не рвут.
    private fun appInForeground(): Flow<Boolean> =
        ProcessLifecycleOwner.get().lifecycle.currentStateFlow
            .map { it.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()

    // Живёт, пока пользователь вошёл и приложение на экране: collectLatest отменит его
    // при выходе, смене аккаунта или уходе в фон.
    private suspend fun runSync(uid: String) = coroutineScope {
        launch { guarded("days") { syncDays(uid) } }
        launch { guarded("schedule") { syncSchedule(uid) } }
    }

    // Выгрузка идёт в отдельной корутине: оффлайн Firestore.await() не завершается,
    // и это не должно задерживать применение входящих изменений.
    private suspend fun syncDays(uid: String) = coroutineScope {
        var initialUploadDone = false
        cloud.observeDays(uid).collect { changes ->
            repository.mergeRemoteDays(changes.upserts)
            // Решение «что выгружать» принимаем по первому серверному снапшоту: кэш может быть устаревшим
            if (!initialUploadDone && !changes.isFromCache) {
                initialUploadDone = true
                val remote = changes.all.associateBy { it.date }
                val toUpload = repository.getAllDays()
                    .filter { it.lastUpdated > 0L } // lastUpdated == 0 — локальная заглушка
                    .filter { local -> remote[local.date.toString()]?.let { local.lastUpdated > it.lastUpdated } ?: true }
                if (toUpload.isNotEmpty()) {
                    launch { cloud.setDays(uid, toUpload.map { it.toDto() }) }
                }
            }
        }
    }

    private suspend fun syncSchedule(uid: String) = coroutineScope {
        var initialUploadDone = false
        cloud.observeSchedule(uid).collect { changes ->
            repository.mergeRemoteSchedule(changes.upserts)
            repository.removeRemoteSchedule(changes.removedIds)
            if (!initialUploadDone && !changes.isFromCache) {
                initialUploadDone = true
                val remote = changes.all.associateBy { it.id }
                val toUpload = repository.getAllSchedule()
                    .filter { local -> remote[local.id]?.let { local.lastUpdated > it.lastUpdated } ?: true }
                if (toUpload.isNotEmpty()) {
                    launch { cloud.setSchedules(uid, toUpload.map { it.toDto() }) }
                }
            }
        }
    }

    private suspend fun guarded(name: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Sync of $name stopped: ${e.message}", e)
        }
    }

    private companion object {
        const val TAG = "SyncManager"
    }
}
