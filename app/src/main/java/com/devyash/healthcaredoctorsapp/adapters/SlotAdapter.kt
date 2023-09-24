package com.devyash.healthcaredoctorsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.AddSlotItemLayoutBinding
import com.devyash.healthcaredoctorsapp.databinding.SlotItemLayoutBinding

class SlotAdapter(private val listOfSlots: List<String>) :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var itemClickListener: ((view: View, position: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewHolder {
        return when (viewType) {
            R.layout.slot_item_layout -> {
                HomeRecyclerViewHolder.SlotViewHolder(
                    SlotItemLayoutBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }

            R.layout.add_slot_item_layout -> {
                HomeRecyclerViewHolder.AddSlotViewHolder(
                    AddSlotItemLayoutBinding.inflate(
                        LayoutInflater.from(
                            parent.context
                        ), parent, false
                    )
                )
            }

            else -> {
                throw IllegalArgumentException("Invalid ViewType Provided")
            }
        }
    }

    override fun getItemCount(): Int {
        return listOfSlots.size
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        val currentSlot = listOfSlots[position]

        holder.binding.tvSlotTiming.text = currentSlot.toString()
    }


}