package com.devyash.healthcaredoctorsapp.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devyash.healthcaredoctorsapp.models.ChatMessage
import com.devyash.healthcaredoctorsapp.models.ChatRoom
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants.RECENTCHATS
import com.devyash.healthcaredoctorsapp.repositories.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val chatRepository: ChatRepository):ViewModel(){
    private val _getOrCreateChatRoom = MutableStateFlow<NetworkResult<ChatRoom>?>(null)
    val getOrCreateChatRoom: StateFlow<NetworkResult<ChatRoom>?> = _getOrCreateChatRoom

    private val _chatMessages = MutableStateFlow<NetworkResult<List<ChatMessage>>?>(null)
    val chatMessages:StateFlow<NetworkResult<List<ChatMessage>>?> = _chatMessages

    private val _sendMessage = MutableStateFlow<NetworkResult<ChatMessage>?>(null)
    val sendMessage:StateFlow<NetworkResult<ChatMessage>?> = _sendMessage

    private val _recentChats = MutableStateFlow<NetworkResult<List<Pair<ChatRoom,String>>>?>(null)
    val recentChats: StateFlow<NetworkResult<List<Pair<ChatRoom,String>>>?> = _recentChats


    fun getOrCreateChatRoom(userId:String) = viewModelScope.launch {
        _getOrCreateChatRoom.value = NetworkResult.Loading()

        try {
            val result = chatRepository.getOrCreateChatRoom(userId)
            result.collect{
                when(it){
                    is NetworkResult.Error ->  _getOrCreateChatRoom.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _getOrCreateChatRoom.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _getOrCreateChatRoom.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _getOrCreateChatRoom.value = NetworkResult.Error(e.message)
        }
    }

    fun getChatMessages() =  viewModelScope.launch {
        _chatMessages.value = NetworkResult.Loading()

        try {
            val result = chatRepository.getChatMessages()
            result.collect{
                when(it){
                    is NetworkResult.Error ->  _chatMessages.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _chatMessages.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _chatMessages.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _chatMessages.value = NetworkResult.Error(e.message)
        }
    }

    fun sendMessageToTheUser(message:String) = viewModelScope.launch {
        _sendMessage.value = NetworkResult.Loading()

        try {
            val result = chatRepository.sendMessageToTheUser(message)
            result.collect{
                when(it){
                    is NetworkResult.Error ->  _sendMessage.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _sendMessage.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _sendMessage.value = NetworkResult.Success(it.data!!)
                }
            }
        }catch (e:Exception){
            _sendMessage.value = NetworkResult.Error(e.message)
        }
    }

    fun getRecentChats() = viewModelScope.launch {
        _recentChats.value = NetworkResult.Loading()
        Log.d(RECENTCHATS,"Entering Viewmodel")
        try {
            val result = chatRepository.getRecentChats()
            result.collect {
                when (it) {
                    is NetworkResult.Error -> _recentChats.value = NetworkResult.Error(it.message)
                    is NetworkResult.Loading -> _recentChats.value = NetworkResult.Loading()
                    is NetworkResult.Success -> _recentChats.value =
                        NetworkResult.Success(it.data!!)
                }
            }

        } catch (e: Exception) {
            Log.d(RECENTCHATS,"Viewmodel Catch block ${e.message.toString()}")
            _recentChats.value = NetworkResult.Error(e.message.toString())
        }
    }
}