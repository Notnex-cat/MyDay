package com.notnex.myday.sync

import android.util.Log
import com.notnex.myday.auth.AuthRepository
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.data.remote.CloudDataSource
import com.notnex.myday.data.remote.toDto
import com.notnex.myday.di.ApplicationScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Жизненный цикл синхронизации, привязанный к аккаунту (а не к какому-либо экрану):
 *  - вход: подписываемся на облако, слияние по lastUpdated в обе стороны;
 *  - выход или смена аккаунта: чистим локальную БД, чтобы данные не перетекли к другому пользователю.
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
            auth.authState
                .map { it.user?.userId }
                .distinctUntilChanged()
                .collectLatest { uid ->
                    if (previousUid != null && previousUid != uid) repository.clearLocal()
                    previousUid = uid
                    if (uid != null) runSync(uid)
                }
        }
    }

    // Живёт, пока пользователь вошёл: collectLatest отменит его при выходе или смене аккаунта.
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
                launch { toUpload.forEach { cloud.setDay(uid, it.toDto()) } }
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
                launch { toUpload.forEach { cloud.setSchedule(uid, it.toDto()) } }
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
