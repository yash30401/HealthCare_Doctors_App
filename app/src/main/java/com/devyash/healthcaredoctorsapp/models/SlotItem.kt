package com.devyash.healthcaredoctorsapp.models

import android.graphics.drawable.Drawable
import android.widget.ImageView

sealed class SlotItem(){

    class slotTiming(val slotTiming:String):SlotItem()

    class allSlots(val timings:List<String>):SlotItem()

    class slotAddButton(val resource: Drawable):SlotItem()
}

