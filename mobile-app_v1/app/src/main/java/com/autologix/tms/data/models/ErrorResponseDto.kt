package com.autologix.tms.data.models

import com.google.gson.annotations.SerializedName

data class ErrorResponseDto(
    @SerializedName("statusCode")
    val statusCode: Int? = null,
    @SerializedName("message")
    val message: Any? = null,
    @SerializedName("error")
    val error: String? = null,
    @SerializedName("timestamp")
    val timestamp: String? = null,
    @SerializedName("path")
    val path: String? = null
) {
    val singleMessage: String
        get() = when (message) {
            is String -> message
            is List<*> -> message.filterNotNull().joinToString(", ")
            else -> error ?: "An unexpected error occurred"
        }

    fun getFormattedMessage(): String = singleMessage
}
