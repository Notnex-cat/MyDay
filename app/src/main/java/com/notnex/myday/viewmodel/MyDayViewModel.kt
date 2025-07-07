package com.notnex.myday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayFirebaseDTO
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.firebase.SignInResult
import com.notnex.myday.firebase.SignInState
import com.notnex.myday.firebase.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    fun getScore(date: LocalDate) = myDayRepository.getScoreByDate(date)

    private val auth = Firebase.auth

    private val fstore = Firebase.firestore

    fun saveDayEntry(date: LocalDate, score: Double, note: String) {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            try {
                val entity = MyDayEntity(
                    date = date,
                    score = score,
                    note = note,
                    lastUpdated = System.currentTimeMillis()
                )

                // 1. Обновляем локально
                myDayRepository.saveOrUpdateDayScore(date, score, note)

                // 2. Подготавливаем DTO
                val dto = MyDayFirebaseDTO(
                    date = date.toString(),
                    score = score,
                    note = note,
                    lastUpdated = entity.lastUpdated
                )

                val docRef = fstore
                    .collection("users")
                    .document(uid)
                    .collection("days")
                    .document(date.toString()) // используем дату как ID

                // 3. Загружаем текущее облачное состояние (если есть)
                val snapshot = docRef.get().await()
                val remote = snapshot.toObject(MyDayFirebaseDTO::class.java)

                // 4. Сравнение по lastUpdated
                if (remote == null || entity.lastUpdated > remote.lastUpdated) {
                    docRef.set(dto, SetOptions.merge()).await()
                    Log.d("Firestore", "Saved to Firestore for user $uid, date $date")
                } else {
                    Log.d("Firestore", "Remote data is newer or same, skip upload")
                }

            } catch (e: Exception) {
                Log.e("Firestore", "Save failed: ${e.message}")
                _saveResult.value = Result.failure(e)
            }
        }
    }




    //Firebase
    // Firebase Auth
    private val _signInState = MutableStateFlow(SignInState())
    val signInState = _signInState.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(null)
    val userData: StateFlow<UserData?> = _userData



    fun onSignInResult(result: SignInResult) {
        _userData.value = result.data
        _signInState.update { it.copy(
            isSignInSuccessful = result.data != null,
            signInError = result.errorMessage
        ) }
    }

    fun resetState(){
        _signInState.update { SignInState() }
    }

    //переделать!!!
    fun setUserIfSignedIn(user: UserData?) {
        if (user != null && _userData.value == null) {
            _userData.value = user
            _signInState.update { it.copy(isSignInSuccessful = true) }
        }
    }

    //Firestore Database

    fun subscribeToUserRealtimeUpdates() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        Firebase.firestore.collection("users")
            .document(uid)
            .collection("days")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Listen failed", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    viewModelScope.launch(Dispatchers.IO) {
                        for (doc in it.documents) {
                            val dto = doc.toObject(MyDayFirebaseDTO::class.java) ?: continue
                            val entity = MyDayEntity(
                                date = LocalDate.parse(dto.date),
                                score = dto.score,
                                note = dto.note,
                                lastUpdated = dto.lastUpdated
                            )
                            myDayRepository.saveOrUpdateDayScore(
                                date = entity.date,
                                score = entity.score,
                                note = entity.note
                            )
                        }
                    }
                }
            }
        }
}