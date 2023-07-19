package com.darkndev.chatroom.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darkndev.chatroom.databinding.LayoutIncomingBinding
import com.darkndev.chatroom.databinding.LayoutOutgoingBinding
import com.darkndev.chatroom.models.Message

class MessageAdapter(private val user: String) :
    ListAdapter<Message, RecyclerView.ViewHolder>(DiffCallback()) {

    class DiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Message, newItem: Message) =
            oldItem == newItem
    }

    override fun getItemViewType(position: Int) = if (user == getItem(position).username) 0 else 1


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        0 -> {
            val binding = LayoutOutgoingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            OutgoingViewHolder(binding)
        }

        else -> {
            val binding = LayoutIncomingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            IncomingViewHolder(binding)
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        if (message != null)
            if (user == message.username)
                (holder as OutgoingViewHolder).bind(message)
            else
                (holder as IncomingViewHolder).bind(message)
    }

    inner class IncomingViewHolder(private val binding: LayoutIncomingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                username.text = message.username
                incomingMessage.text = message.text
                timestamp.text = message.formattedTime
            }
        }
    }

    inner class OutgoingViewHolder(private val binding: LayoutOutgoingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: Message) {
            binding.apply {
                outgoingMessage.text = message.text
                timestamp.text = message.formattedTime
            }
        }
    }
}