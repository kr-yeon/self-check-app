package com.msub.selfcheck.recyclerview

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msub.selfcheck.R

class MyViewHolder(view: View):RecyclerView.ViewHolder(view) {
    val info=view.findViewById<TextView>(R.id.infotext)
    val delbtn=view.findViewById<Button>(R.id.delbtn)
}