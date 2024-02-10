package com.devyash.healthcaredoctorsapp.models.MessageModel

import com.devyash.healthcaredoctorsapp.VideoCalling.models.TYPE

data class MessageModel(
    val type: TYPE,
    val name: String? = null,
    val target: String? = null,
    val data:Any?=null
)
