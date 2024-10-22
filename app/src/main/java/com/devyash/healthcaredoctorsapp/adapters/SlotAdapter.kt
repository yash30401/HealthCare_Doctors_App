package com.devyash.healthcaredoctorsapp.adapters

import android.app.TimePickerDialog.OnTimeSetListener
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker.OnTimeChangedListener
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.devyash.healthcaredoctorsapp.R
import com.devyash.healthcaredoctorsapp.databinding.AddSlotItemLayoutBinding
import com.devyash.healthcaredoctorsapp.databinding.SlotItemLayoutBinding
import com.devyash.healthcaredoctorsapp.models.SlotItem

class SlotAdapter(addButton:Drawable) :
    RecyclerView.Adapter<HomeRecyclerViewHolder>() {

    var itemClickListener: ((view: View, position: Int) -> Unit)? = null
    var deleteClickListner:((view: View, position: Int) -> Unit)? = null
    private val listOfSlots=  mutableListOf<SlotItem>(SlotItem.slotAddButton(addButton))
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

    fun showFirebaseList(slots:List<Long>){
        for (slot in slots) {
            addItemToTheList(slot)
        }
    }

    fun addItemToTheList(slotTiming:Long):Int{
        val newItem = SlotItem.slotTiming(slotTiming)
        val slotIndex = listOfSlots.size-1
        listOfSlots.add(slotIndex,newItem)
        notifyItemInserted(slotIndex)

        return slotIndex
    }


    fun deleteSlot(position: Int){
        listOfSlots.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewHolder, position: Int) {
        holder.itemClickListener = itemClickListener
        holder.deleteClickListner = deleteClickListner
        when(holder){
            is HomeRecyclerViewHolder.AddSlotViewHolder -> {
                holder.bind(listOfSlots[position] as SlotItem.slotAddButton)
            }
            is HomeRecyclerViewHolder.SlotViewHolder -> {
                holder.bind(listOfSlots[position] as SlotItem.slotTiming)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (listOfSlots[position]) {
            is SlotItem.slotTiming->R.layout.slot_item_layout
            is SlotItem.slotAddButton->R.layout.add_slot_item_layout
            else -> {
                0
            }
        }
    }

}