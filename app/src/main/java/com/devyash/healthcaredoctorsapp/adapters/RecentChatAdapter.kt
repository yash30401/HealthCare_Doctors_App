package com.devyash.healthcaredoctorsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.RecentChatItemLayoutBinding
import com.devyash.healthcaredoctorsapp.models.ChatRoom
import com.devyash.healthcaredoctorsapp.utils.DoctorDiffUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecentChatAdapter: RecyclerView.Adapter<RecentChatAdapter.RecentChatViewHolder>() {

    private val asyncListDiffer =
        AsyncListDiffer<Pair<ChatRoom, String>>(this, DoctorDiffUtil())

    inner class RecentChatViewHolder(itemview: View) : RecyclerView.ViewHolder(itemview) {
        val binding = RecentChatItemLayoutBinding.bind(itemview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentChatViewHolder {
        val viewHolder = RecentChatViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recent_chat_item_layout, parent, false)
        )

        return viewHolder
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    override fun onBindViewHolder(holder: RecentChatViewHolder, position: Int) {
        val recentChat = asyncListDiffer.currentList[position]

        holder.binding.tvUsername.text = recentChat.second.subSequence(0,5).toString()+"..."
        holder.binding.tvLastMesaage.text = recentChat.first.lastMessage
        holder.binding.tvLastTimeStamp.text =
            convertTimestampToTimeString(recentChat.first.lastMessageTimestamp.toDate().time)

    }

    fun convertTimestampToTimeString(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    fun setNewRecentChat(newList: List<Pair<ChatRoom, String>>) {
        asyncListDiffer.submitList(newList)
    }
}