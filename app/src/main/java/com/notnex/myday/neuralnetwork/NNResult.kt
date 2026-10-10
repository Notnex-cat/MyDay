package com.notnex.myday.neuralnetwork

sealed class NNResult<out T> {
    data object Idle : NNResult<Nothing>()
    data object Loading : NNResult<Nothing>()
    data class Success<out T>(val data: T) : NNResult<T>()
    data class Error(val exception: Throwable) : NNResult<Nothing>()
}
