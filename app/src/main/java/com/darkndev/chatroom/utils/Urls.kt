package com.darkndev.chatroom.utils

import com.darkndev.chatroom.BuildConfig

object Urls {
    private const val SOCKET = "ws://"
    private const val HTTPS = "http://"
    private const val BASE_URL = BuildConfig.BASE_URL
    const val MESSAGES = "$HTTPS$BASE_URL/messages"
    const val CHAT = "$SOCKET$BASE_URL/chat"
}