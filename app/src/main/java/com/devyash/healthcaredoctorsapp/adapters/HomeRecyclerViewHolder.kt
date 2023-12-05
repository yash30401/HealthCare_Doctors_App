package com.devyash.healthcaredoctorsapp.adapters

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.devyash.healthcaredoctorsapp.databinding.AddSlotItemLayoutBinding
import com.devyash.healthcaredoctorsapp.databinding.SlotItemLayoutBinding
import com.devyash.healthcaredoctorsapp.models.SlotItem
import java.text.SimpleDateFormat
import java.util.Date

sealed class HomeRecyclerViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    var itemClickListener: ((view: View, position: Int) -> Unit)? = null
    var deleteClickListner: ((view:View,position:Int)-> Unit)?= null
    class SlotViewHolder(private val binding:SlotItemLayoutBinding):HomeRecyclerViewHolder(binding){
        fun bind(slotTiming:SlotItem.slotTiming){
            val simpleDateFormat = SimpleDateFormat("dd,MMMM yyyy HH:mm a")
            val formattedDate = simpleDateFormat.format(slotTiming.slotTiming)

            binding.tvSlotTiming.text = formattedDate.toString()
            binding.ivDelete.setOnClickListener {
                deleteClickListner?.invoke(it,adapterPosition)
            }
        }
    }


    class AddSlotViewHolder(private val binding:AddSlotItemLayoutBinding):HomeRecyclerViewHolder(binding){
        fun bind(buttonImage:SlotItem.slotAddButton){
            binding.ivAddSlot.setImageDrawable(buttonImage.resource)
            binding.ivAddSlot.setOnClickListener {
                itemClickListener?.invoke(it,adapterPosition)
            }
        }
    }
}