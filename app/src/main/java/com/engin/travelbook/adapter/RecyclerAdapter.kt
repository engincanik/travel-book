package com.engin.travelbook.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.engin.travelbook.MapsActivity
import com.engin.travelbook.Place
import com.engin.travelbook.R

class RecyclerAdapter(private var places: List<Place>): RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.place_name)
        val latitudeVal: TextView = itemView.findViewById(R.id.latitude_value)
        val longitudeVal: TextView = itemView.findViewById(R.id.longitude_value)

        init {
            itemView.setOnClickListener { v: View ->
                val position: Int = adapterPosition
                val intent = Intent(v.context, MapsActivity::class.java)
                intent.putExtra("selectedPlace", places[position])
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.place_card, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.placeName.text = places[position].address
        holder.latitudeVal.text = places[position].latitude.toString()
        holder.longitudeVal.text = places[position].longitude.toString()
    }

    override fun getItemCount(): Int {
        return places.size
    }
}