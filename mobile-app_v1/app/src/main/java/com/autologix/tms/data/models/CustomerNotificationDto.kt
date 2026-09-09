package com.autologix.tms.data.models

import com.google.gson.annotations.SerializedName

data class CustomerNotificationDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("isRead")
    val isRead: Boolean = false,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("targetId")
    val targetId: String? = null
)
