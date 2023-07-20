package com.darkndev.chatroom.data

import androidx.room.*
import com.darkndev.chatroom.models.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Upsert
    suspend fun insertAll(vararg messages: Message)

    @Query("DELETE FROM message_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM message_table ORDER BY timestamp")
    fun getAllMessages(): Flow<List<Message>>

}