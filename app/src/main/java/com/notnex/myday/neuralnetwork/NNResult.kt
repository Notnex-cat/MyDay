package com.notnex.myday.neuralnetwork

sealed class NNResult<out T> {
    data class Success<out T>(val data: T) : NNResult<T>()
    data class Error(val exception: Throwable) : NNResult<Nothing>()
    object Loading : NNResult<Nothing>()
}

