package com.darkndev.chatroom.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.darkndev.chatroom.utils.getFormatTime
import kotlinx.serialization.Serializable

@Entity(tableName = "message_table")
@Serializable
data class Message(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val text: String,
    val username: String,
    val timestamp: Long
) {
    fun toTime() = getFormatTime(timestamp)
}
