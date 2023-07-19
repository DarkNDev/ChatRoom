package com.darkndev.chatroom.data

import com.darkndev.chatroom.models.Message
import com.darkndev.chatroom.models.MessageDto
import com.darkndev.chatroom.utils.Resource
import com.darkndev.chatroom.utils.Urls.CHAT
import com.darkndev.chatroom.utils.Urls.MESSAGES
import com.darkndev.chatroom.utils.httpResponse
import com.darkndev.chatroom.utils.socketResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ChatApi @Inject constructor(
    private val client: HttpClient
) {

    private var socket: WebSocketSession? = null

    suspend fun getAllMessages(): Resource<List<Message>> = httpResponse(
        request = {
            client.get {
                url(MESSAGES)
            }
        },
        success = { response ->
            val messages = response.body<List<MessageDto>>()
                .map { it.toMessage() }
            Resource.Success(messages)
        },
        error = { _, _, e ->
            Resource.Error(e)
        }
    )

    suspend fun initSession(username: String): Resource<Unit> = socketResponse(
        request = {
            client.webSocketSession {
                url(CHAT)
                parameter("username", username)
            }
        },
        socket = {
            if (it.isActive) {
                socket = it
                Resource.Success(Unit)
            } else {
                Resource.Error(Throwable("Couldn't establish a connection"))
            }
        },
        error = { e ->
            e.printStackTrace()
            Resource.Error(e)
        }
    )

    suspend fun sendMessage(message: String) {
        try {
            socket?.send(Frame.Text(message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun observeMessages(): Flow<Message> = try {
        socket?.incoming
            ?.receiveAsFlow()
            ?.filter { it is Frame.Text }
            ?.map {
                val json = (it as? Frame.Text)?.readText() ?: ""
                val messageDto = Json.decodeFromString<MessageDto>(json)
                messageDto.toMessage()
            } ?: flow { }
    } catch (e: Exception) {
        e.printStackTrace()
        flow { }
    }

    suspend fun closeSession() {
        try {
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}