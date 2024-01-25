package com.devyash.healthcaredoctorsapp.VideoCalling.utils

import com.devyash.healthcaredoctorsapp.models.MessageModel.MessageModel


interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}