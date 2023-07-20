package com.darkndev.chatroom.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.darkndev.chatroom.models.Message

@Database(
    entities = [Message::class],
    version = 1,
    exportSchema = false
)
abstract class Database : RoomDatabase() {

    abstract fun messageDao(): MessageDao

}