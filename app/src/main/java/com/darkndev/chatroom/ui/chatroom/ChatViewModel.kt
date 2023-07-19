package com.darkndev.chatroom.ui.chatroom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.darkndev.chatroom.data.ChatApi
import com.darkndev.chatroom.models.Chatroom
import com.darkndev.chatroom.models.Message
import com.darkndev.chatroom.utils.Connection
import com.darkndev.chatroom.utils.NetworkConnectivityObserver
import com.darkndev.chatroom.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
    private val state: SavedStateHandle,
    networkConnectivityObserver: NetworkConnectivityObserver
) : ViewModel() {

    val username = state.get<String>("username")!!

    var messageText = state.get<String>("MESSAGE") ?: ""
        set(value) {
            field = value
            state["MESSAGE"] = value
        }

    private val _messages = MutableStateFlow(listOf<Message>())
    val messages = _messages.asLiveData(viewModelScope.coroutineContext)

    private val _chatroom = MutableStateFlow(Chatroom())
    val chatroom = _chatroom.asStateFlow()

    private val networkObserver = networkConnectivityObserver.observe()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        viewModelScope.launch {
            networkObserver.collectLatest { connection ->
                connection ?: return@collectLatest
                _chatroom.value = _chatroom.value.copy(loading = true, roomConnected = false)
                if (connection == Connection.Available) {
                    delay(5000)
                    getAllMessages()
                } else {
                    _chatroom.value = _chatroom.value.copy(loading = false, roomConnected = false)
                    channel.send(Event.ShowMessage("Check Internet Connection"))
                }
            }
        }
    }

    private suspend fun getAllMessages() {
        when (val result = chatApi.getAllMessages()) {
            is Resource.Error -> {
                _chatroom.value = _chatroom.value.copy(loading = false, roomConnected = false)
                channel.send(Event.ShowMessage("Error Retrieving Messages"))
            }

            is Resource.Success -> {
                _messages.value = result.data!!
                initialiseSession()
            }
        }
    }

    private suspend fun initialiseSession() {
        when (chatApi.initSession(username)) {
            is Resource.Error -> {
                _chatroom.value = _chatroom.value.copy(loading = false, roomConnected = false)
                channel.send(Event.ShowMessage("Error Joining Chatroom"))
            }

            is Resource.Success -> {
                _chatroom.value = _chatroom.value.copy(loading = false, roomConnected = true)
                chatApi.observeMessages()
                    .onEach { message ->
                        val newList = _messages.value.toMutableList().apply {
                            add(message)
                        }
                        _messages.value = newList
                    }.launchIn(viewModelScope)
            }
        }
    }

    private fun disconnectFromChat() = viewModelScope.launch {
        chatApi.closeSession()
        _chatroom.value = _chatroom.value.copy(loading = false, roomConnected = false)
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
        data class ShowMessage(val message: String) : Event()
    }

    private val channel = Channel<Event>()
    val receive = channel.receiveAsFlow()
}