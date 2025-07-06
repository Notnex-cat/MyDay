package com.notnex.myday.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.notnex.myday.data.MyDayEntity
import com.notnex.myday.data.MyDayFirebaseDTO
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.firebase.SignInResult
import com.notnex.myday.firebase.SignInState
import com.notnex.myday.firebase.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val myDayRepository: MyDayRepository,
) : ViewModel() {

    //Database
    private val _saveResult = MutableStateFlow(Result.success(Unit))

    fun getScore(date: LocalDate) = myDayRepository.getScoreByDate(date)

    fun saveDayEntry(date: LocalDate, score: Double, note: String) {
        viewModelScope.launch {
            try {
                myDayRepository.saveOrUpdateDayScore(date, score, note)
                _saveResult.value = Result.success(Unit)
                saveDataToFirestore(MyDayEntity(date = date, score = score, note = note))
            } catch (e: Exception) {
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
    private val fstore = Firebase.firestore.collection("user_days")

    private fun saveDataToFirestore(userDay: MyDayEntity) = CoroutineScope(Dispatchers.IO).launch {
        val dto = MyDayFirebaseDTO(
            date = userDay.date.toString(), // сериализуем LocalDate как строку
            score = userDay.score,
            note = userDay.note
        )
        try {
            fstore.add(dto).await()
            withContext(Dispatchers.Main) {
                Log.d("Firestore", "Saved to Firestore")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.d("Firestore", e.message.toString())
            }
        }
    }

    fun subscribeToRealtimeUpdates() {
        fstore.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.d("Firestore", "Error: ${it.message}")
                return@addSnapshotListener
            }

            querySnapshot?.let { snapshot ->
                viewModelScope.launch(Dispatchers.IO) {
                    for (document in snapshot.documents) {
                        try {
                            val dto = document.toObject(MyDayFirebaseDTO::class.java)
                            if (dto != null) {
                                val entity = MyDayEntity(
                                    date = LocalDate.parse(dto.date),
                                    score = dto.score,
                                    note = dto.note
                                )
                                myDayRepository.saveOrUpdateDayScore(
                                    date = entity.date,
                                    score = entity.score,
                                    note = entity.note
                                )
                            }
                        } catch (e: Exception) {
                            Log.d("Firestore", "Parsing error: ${e.message}")
                        }
                    }
                    Log.d("Firestore", "Synced real-time updates to Room")
                }
            }
        }
    }
}