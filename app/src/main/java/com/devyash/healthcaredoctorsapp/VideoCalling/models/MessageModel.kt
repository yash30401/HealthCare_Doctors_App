package com.devyash.healthcaredoctorsapp.models.MessageModel

data class MessageModel(
    val type: String,
    val name: String? = null,
    val target: String? = null,
    val data:Any?=null
)
