package com.autologix.tms.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(
        val message: String,
        val statusCode: Int? = null,
        val errorBody: String? = null
    ) : NetworkResult<Nothing>()
    data class NetworkError(val exception: Throwable? = null) : NetworkResult<Nothing>()
    data object Loading : NetworkResult<Nothing>()
}
