package com.darkndev.chatroom.models

import kotlinx.serialization.Serializable
import java.text.DateFormat
import java.util.Date

@Serializable
data class MessageDto(
    val id: Int,
    val text: String,
    val username: String,
    val timestamp: Long
) {
    fun toMessage(): Message {
        val date = Date(timestamp)
        val formattedDate = DateFormat
            .getDateInstance(DateFormat.DEFAULT)
            .format(date)
        return Message(
            id = id,
            text = text,
            formattedTime = formattedDate,
            username = username
        )
    }
}
