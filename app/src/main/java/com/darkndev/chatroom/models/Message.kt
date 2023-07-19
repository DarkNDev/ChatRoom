package com.darkndev.chatroom.models

data class Message(
    val id: Int,
    val text: String,
    val formattedTime: String,
    val username: String
)
