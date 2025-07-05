package com.notnex.myday.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notnex.myday.data.MyDayRepository
import com.notnex.myday.firebase.SignInResult
import com.notnex.myday.firebase.SignInState
import com.notnex.myday.firebase.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MyDayViewModel @Inject constructor(
    private val myDayRepository: MyDayRepository,
) : ViewModel() {


    //DataBase
    private val _saveResult = MutableStateFlow(Result.success(Unit))

    fun getScore(date: LocalDate) = myDayRepository.getScoreByDate(date)

    fun saveDayEntry(date: LocalDate, score: Double, note: String) {
        viewModelScope.launch {
            try {
                myDayRepository.saveOrUpdateDayScore(date, score, note)
                _saveResult.value = Result.success(Unit)
            } catch (e: Exception) {
                _saveResult.value = Result.failure(e)
            }
        }
    }


    //Firebase
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
}