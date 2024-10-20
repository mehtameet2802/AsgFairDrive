package com.example.assignment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.example.assignment.models.Ride


class RideAdapter(
    private var rideList:List<Ride>,
):RecyclerView.Adapter<RideAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){

        fun bind(currentRide:Ride) {
            val source = itemView.findViewById<TextView>(R.id.text_source)
            val destination = itemView.findViewById<TextView>(R.id.text_destination)
            val startTime = itemView.findViewById<TextView>(R.id.text_start_time)

            itemView.apply {
                source.text = currentRide.pickup
                destination.text = currentRide.drop
                startTime.text = currentRide.timestamp
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.rv_item_layout,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return rideList.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(rideList[position])
    }

    fun updateRides(newItems:List<Ride>){
        rideList = newItems
        notifyDataSetChanged()
    }

}