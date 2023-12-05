package com.devyash.healthcaredoctorsapp.models

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.google.firebase.Timestamp

sealed class SlotItem(){

    class slotTiming(val slotTiming:Long):SlotItem()

    class allSlots(val timings:List<String>):SlotItem()

    class slotAddButton(val resource: Drawable):SlotItem()
}

