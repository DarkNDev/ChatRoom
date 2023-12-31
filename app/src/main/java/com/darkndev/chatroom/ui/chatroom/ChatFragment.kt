package com.darkndev.chatroom.ui.chatroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.darkndev.chatroom.adapters.MessageAdapter
import com.darkndev.chatroom.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    //comes from navigation dependency
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        val messageAdapter = MessageAdapter(viewModel.username)

        binding.apply {
            recyclerView.apply {
                adapter = messageAdapter
            }

            messageText.setText(viewModel.messageText)

            messageText.doAfterTextChanged {
                viewModel.messageText = it.toString()
            }

            messageLayout.setEndIconOnClickListener {
                viewModel.sendMessage()
                messageText.setText("")
            }

            viewModel.messages.observe(viewLifecycleOwner) {
                messageAdapter.submitList(it) {
                    recyclerView.scrollToPosition(it.size - 1)
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.receive.collectLatest { event ->
                    when (event) {
                        is ChatViewModel.Event.Status -> {
                            event.chatroom.message?.let {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                            }
                            if (event.chatroom.loading) progress.show() else progress.hide()
                            messageLayout.isEnabled = event.chatroom.roomConnected
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}