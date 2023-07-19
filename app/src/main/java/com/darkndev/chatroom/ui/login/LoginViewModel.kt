package com.darkndev.chatroom.ui.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val state: SavedStateHandle
) : ViewModel() {

    var usernameText = state.get<String>("USERNAME") ?: ""
        set(value) {
            field = value
            state["USERNAME"] = value
        }

    fun signIn() = viewModelScope.launch {
        if (usernameText.isNotBlank())
            channel.send(Event.NavigateToChatRoom)
        else
            channel.send(Event.ShowMessage("Username is Blank"))
    }

    sealed class Event {
        object NavigateToChatRoom : Event()
        data class ShowMessage(val message: String) : Event()
    }

    private val channel = Channel<Event>()
    val receive = channel.receiveAsFlow()
}