package com.darkndev.chatroom.utils

object Urls {
    private const val SOCKET = "ws://"
    private const val HTTPS = "http://"
    private const val BASE_URL = "192.168.1.10:8080"
    const val MESSAGES = "$HTTPS$BASE_URL/messages"
    const val CHAT = "$SOCKET$BASE_URL/chat"
}