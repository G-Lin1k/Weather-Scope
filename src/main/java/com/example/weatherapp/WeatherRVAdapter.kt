package com.example.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class WeatherRVAdapter(private val context: Context, private val weatherRVModelArrayList: ArrayList<WeatherRVModel>) :
    RecyclerView.Adapter<WeatherRVAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.weather_rv_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val modal = weatherRVModelArrayList[position]
        holder.temperatureTV.text = "${modal.temperature}°C"
        Picasso.get().load("http:${modal.icon}").into(holder.conditionIV)
        holder.windTV.text = "${modal.windspeed}Km/h"
        val input = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val output = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        try {
            val t: Date = input.parse(modal.time)
            holder.timeTV.text = output.format(t)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return weatherRVModelArrayList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var windTV: TextView = itemView.findViewById(R.id.idTVWindSpeed)
        var temperatureTV: TextView = itemView.findViewById(R.id.idTVTemperature)
        var timeTV: TextView = itemView.findViewById(R.id.idTVTime)
        var conditionIV: ImageView = itemView.findViewById(R.id.idTVCondition)
    }
}
