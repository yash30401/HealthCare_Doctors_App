package com.devyash.healthcaredoctorsapp.models

import com.google.firebase.Timestamp

data class ChatMessage(
    val message: String,
    val senderId: String,
    val timestamp: Timestamp
)
