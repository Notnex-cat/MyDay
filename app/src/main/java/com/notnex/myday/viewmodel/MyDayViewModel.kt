package com.notnex.myday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayFirebaseDTO
import com.notnex.myday.data.MyDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val myDayRepository: MyDayRepository,
) : ViewModel() {

    //Database
    private val _saveResult = MutableStateFlow(Result.success(Unit))

    fun getScore(date: LocalDate) = myDayRepository.getEntityByDate(date)

    private val auth = Firebase.auth

    private val fstore = Firebase.firestore

    fun saveDayEntry(date: LocalDate, score: Double, note: String, aiFeedback: String) {
        viewModelScope.launch {
            try {
                val entity = MyDayEntity(
                    date = date,
                    score = score,
                    note = note,
                    lastUpdated = System.currentTimeMillis(),
                    aiFeedback = aiFeedback
                )
                myDayRepository.saveOrUpdateDayEntity(date, score, note, aiFeedback) // сохранение в базе данных локально

                // 2. В Firestore только если пользователь авторизован
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    sendToFirestore(entity, uid)
                } else {
                    Log.d("Firestore", "User not authenticated, skipping Firestore save")
                }

            } catch (e: Exception) {
                Log.e("Firestore", "Save failed: ${e.message}")
                _saveResult.value = Result.failure(e)
            }
        }
    }

    private suspend fun sendToFirestore(entity: MyDayEntity, uid: String) {
        // Подготавливаем DTO
        val dto = MyDayFirebaseDTO(
            date = entity.date.toString(),
            score = entity.score,
            note = entity.note,
            lastUpdated = entity.lastUpdated,
            aiFeedback = entity.aiFeedback
        )

        val docRef = fstore
            .collection("users")
            .document(uid)
            .collection("days")
            .document(entity.date.toString())

        // Загружаем текущее облачное состояние (если есть)
        val snapshot = docRef.get().await()
        val remote = snapshot.toObject(MyDayFirebaseDTO::class.java)

        // Сравнение по lastUpdated
        if (remote == null || entity.lastUpdated > remote.lastUpdated) {
            docRef.set(dto, SetOptions.merge()).await()
            Log.d("Firestore", "Saved to Firestore for user $uid, date ${entity.date}")
        } else {
            Log.d("Firestore", "Remote data is newer or same, skip upload")
        }
    }

    //Firebase
    //Firestore Database
    fun subscribeToUserRealtimeUpdates() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch(Dispatchers.IO) {
            firestoreUserDaysFlow(uid).collect { dtoList ->
                for (dto in dtoList) {
                    val remoteEntity = MyDayEntity(
                        date = LocalDate.parse(dto.date),
                        score = dto.score,
                        note = dto.note,
                        lastUpdated = dto.lastUpdated,
                        aiFeedback = dto.aiFeedback
                    )

                    val localEntity = myDayRepository.getEntityByDate(remoteEntity.date).firstOrNull()

                    if (remoteEntity.lastUpdated > (localEntity?.lastUpdated ?: 0L)) {
                        myDayRepository.saveOrUpdateDayEntity(
                            date = remoteEntity.date,
                            score = remoteEntity.score,
                            note = remoteEntity.note,
                            aiFeedback = remoteEntity.aiFeedback
                        )
                    } else {
                        //Log.d("Firestore", "Local data is newer or same, skip update for ${remoteEntity.date}")
                    }
                }
            }
        }
    }

    private fun firestoreUserDaysFlow(uid: String) = callbackFlow<List<MyDayFirebaseDTO>> {
        val listener = fstore.collection("users")
            .document(uid)
            .collection("days")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error) // Закрываем Flow при ошибке
                    return@addSnapshotListener
                }

                val dtoList = snapshot?.documents
                    ?.mapNotNull { it.toObject(MyDayFirebaseDTO::class.java) }
                    ?: emptyList()

                trySend(dtoList).isSuccess
            }

        awaitClose { listener.remove() } // Удаляем listener при отмене подписки
    }


    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate
    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    // Синхронизация локальных данных с облаком при входе в аккаунт
    fun syncLocalDataToCloud() {
        viewModelScope.launch {
            try {
                // Получаем все локальные записи
                val localEntries = myDayRepository.getAllLocalEntries()

                // Просто вызываем saveDayEntry для каждой записи
                // Это автоматически обработает логику сравнения и сохранения
                for (entry in localEntries) {
                    saveDayEntry(entry.date, entry.score, entry.note, entry.aiFeedback)
                }

                Log.d("Sync", "Local to cloud sync completed")
            } catch (e: Exception) {
                Log.e("Sync", "Sync failed: ${e.message}")
            }
        }
    }
}