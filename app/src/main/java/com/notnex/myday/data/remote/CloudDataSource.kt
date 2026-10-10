package com.notnex.myday.data.remote

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Пачка изменений из снапшота Firestore. */
class RemoteChanges<T>(
    val upserts: List<T>,
    val removedIds: List<String>,
    /** true, пока данные взяты из локального кэша Firestore, а не с сервера. */
    val isFromCache: Boolean,
    allDocuments: () -> List<T>
) {
    /** Все документы коллекции на момент снапшота (считается лениво). */
    val all: List<T> by lazy(allDocuments)
}

/**
 * Единственное место, которое знает про структуру Firestore:
 *  users/{uid}/days/{yyyy-MM-dd}
 *  users/{uid}/schedule/{id}   (плоская коллекция, дата лежит в поле date)
 */
@Singleton
class CloudDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun days(uid: String) =
        firestore.collection("users").document(uid).collection("days")

    private fun schedule(uid: String) =
        firestore.collection("users").document(uid).collection("schedule")

    suspend fun setDay(uid: String, dto: MyDayFirebaseDTO) {
        days(uid).document(dto.date).set(dto).await()
    }

    suspend fun setSchedule(uid: String, dto: MyScheduleFirebaseDTO) {
        schedule(uid).document(dto.id).set(dto).await()
    }

    suspend fun deleteSchedule(uid: String, id: String) {
        schedule(uid).document(id).delete().await()
    }

    fun observeDays(uid: String): Flow<RemoteChanges<MyDayFirebaseDTO>> =
        observe(days(uid), MyDayFirebaseDTO::class.java)

    fun observeSchedule(uid: String): Flow<RemoteChanges<MyScheduleFirebaseDTO>> =
        observe(schedule(uid), MyScheduleFirebaseDTO::class.java)

    // Первый снапшот содержит все документы как ADDED, дальше приходят только дельты,
    // поэтому одного слушателя достаточно и для начальной загрузки, и для realtime.
    private fun <T : Any> observe(
        query: Query,
        clazz: Class<T>
    ): Flow<RemoteChanges<T>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            val upserts = mutableListOf<T>()
            val removed = mutableListOf<String>()
            for (change in snapshot.documentChanges) {
                if (change.type == DocumentChange.Type.REMOVED) {
                    removed += change.document.id
                } else {
                    upserts += change.document.toObject(clazz)
                }
            }
            // Канал безлимитный (см. buffer ниже), так что trySend не потеряет дельту.
            trySend(
                RemoteChanges(
                    upserts = upserts,
                    removedIds = removed,
                    isFromCache = snapshot.metadata.isFromCache,
                    allDocuments = { snapshot.toObjects(clazz) }
                )
            )
        }
        awaitClose { registration.remove() }
    }.buffer(Channel.UNLIMITED)
}
