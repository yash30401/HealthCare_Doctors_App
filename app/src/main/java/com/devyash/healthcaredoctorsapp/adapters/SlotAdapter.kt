package com.devyash.healthcaredoctorsapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.SlotItemLayoutBinding

class SlotAdapter(private val listOfSlots:List<String>):RecyclerView.Adapter<SlotAdapter.SlotViewHolder>() {


    inner class SlotViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val binding = SlotItemLayoutBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val viewHolder = SlotViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.slot_item_layout,null,false))

        return viewHolder
    }

    override fun getItemCount(): Int {
        return listOfSlots.size
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val currentSlot = listOfSlots[position]

       holder.binding.tvSlotTiming.text = currentSlot.toString()
    }

}