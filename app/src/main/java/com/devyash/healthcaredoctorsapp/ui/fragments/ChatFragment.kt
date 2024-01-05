package com.devyash.healthcaredoctorsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.adapters.RecentChatAdapter
import com.devyash.healthcaredoctorsapp.databinding.FragmentChatBinding
import com.devyash.healthcaredoctorsapp.models.DetailedDoctorAppointment
import com.devyash.healthcaredoctorsapp.networking.NetworkResult
import com.devyash.healthcaredoctorsapp.others.Constants.RECENTCHATS
import com.devyash.healthcaredoctorsapp.others.OnRecentChatClickListner
import com.devyash.healthcaredoctorsapp.viewmodels.ChatViewModel
import com.google.firebase.firestore.DocumentReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class ChatFragment : Fragment(R.layout.fragment_chat),OnRecentChatClickListner {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    lateinit var recentChatAdapter: RecentChatAdapter

    private val chatViewModel by viewModels<ChatViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        setupRecentChatRecylerView()
    }

    private fun setupRecentChatRecylerView() {
        recentChatAdapter = RecentChatAdapter(this)
        binding.rvChats.apply {
            adapter = recentChatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        getRecentChats()
    }

    private fun getRecentChats() {
        Log.d(RECENTCHATS,"Entering Function")
        lifecycleScope.launch {
            chatViewModel.getRecentChats()
            chatViewModel.recentChats.collect {
                when (it) {
                    is NetworkResult.Error -> {
                        Log.d(RECENTCHATS, "Error Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Loading -> {
                        Log.d(RECENTCHATS, "Loading Block:- ${it.message.toString()}")
                    }

                    is NetworkResult.Success -> {
                        Log.d(RECENTCHATS, "Success Block:- ${it.data.toString()}")
                        withContext(Dispatchers.Main){
                            recentChatAdapter.setNewRecentChat(it.data!!)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    override fun onClick(second: String) {
        val action = ChatFragmentDirections.actionChatFragmentToChattingFragment(
            second
        )
        findNavController().navigate(action)
    }
}