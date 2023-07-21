package com.darkndev.chatroom.ui.chatroom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.darkndev.chatroom.data.ChatApi
import com.darkndev.chatroom.data.Database
import com.darkndev.chatroom.di.ApplicationScope
import com.darkndev.chatroom.models.Chatroom
import com.darkndev.chatroom.utils.Connection
import com.darkndev.chatroom.utils.NetworkConnectivityObserver
import com.darkndev.chatroom.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatApi: ChatApi,
    private val database: Database,
    private val state: SavedStateHandle,
    networkConnectivityObserver: NetworkConnectivityObserver,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    private val messageDao = database.messageDao()

    val username = state.get<String>("username")!!

    var messageText = state.get<String>("MESSAGE") ?: ""
        set(value) {
            field = value
            state["MESSAGE"] = value
        }

    val messages = messageDao.getAllMessages().asLiveData(viewModelScope.coroutineContext)

    private val networkObserver = networkConnectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            networkObserver.collectLatest { connection ->
                connection ?: return@collectLatest
                channel.send(
                    Event.Status(
                        Chatroom(
                            message = null,
                            loading = true,
                            roomConnected = false
                        )
                    )
                )
                if (connection == Connection.Available) {
                    delay(2000)
                    getAllMessages()
                    initialiseSession()
                } else {
                    channel.send(
                        Event.Status(
                            Chatroom(
                                message = "Check Internet Connection",
                                loading = false,
                                roomConnected = false
                            )
                        )
                    )
                }
            }
        }
    }

    private suspend fun getAllMessages() {
        when (val result = chatApi.getAllMessages()) {
            is Resource.Error -> {
                channel.send(
                    Event.Status(
                        Chatroom(
                            message = "Error Retrieving Messages",
                            loading = false,
                            roomConnected = false
                        )
                    )
                )
            }

            is Resource.Success -> {
                result.data?.let {
                    database.withTransaction {
                        messageDao.deleteAll()
                        messageDao.insertAll(*it.toTypedArray())
                    }
                }
            }
        }
    }

    private suspend fun initialiseSession() {
        when (chatApi.initSession(username)) {
            is Resource.Error -> {
                channel.send(
                    Event.Status(
                        Chatroom(
                            message = "Error Joining Chatroom",
                            loading = false,
                            roomConnected = false
                        )
                    )
                )
            }

            is Resource.Success -> {
                channel.send(
                    Event.Status(
                        Chatroom(
                            message = null,
                            loading = false,
                            roomConnected = true
                        )
                    )
                )
                chatApi.observeMessages()
                    .onEach { message ->
                        messageDao.insertAll(message)
                    }.launchIn(viewModelScope)
            }
        }
    }

    private fun disconnectFromChat() = applicationScope.launch {
        chatApi.closeSession()
        channel.send(
            Event.Status(
                Chatroom(
                    message = null,
                    loading = false,
                    roomConnected = false
                )
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        disconnectFromChat()
    }

    fun sendMessage() = viewModelScope.launch {
        if (messageText.isNotBlank())
            chatApi.sendMessage(messageText)
    }

    sealed class Event {
        data class Status(val chatroom: Chatroom) : Event()
    }

    private val channel = Channel<Event>()
    val receive = channel.receiveAsFlow()
}