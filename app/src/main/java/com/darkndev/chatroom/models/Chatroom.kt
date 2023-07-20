package com.darkndev.chatroom.models

data class Chatroom(
    val message: String?,
    val loading: Boolean = true,
    val roomConnected: Boolean = false
)