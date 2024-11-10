package com.salati

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PrayerTimeAdapter : RecyclerView.Adapter<PrayerTimeAdapter.PrayerTimeViewHolder>() {
    private var prayers: List<PrayerTime> = emptyList()

    fun updatePrayers(newPrayers: List<PrayerTime>) {
        prayers = newPrayers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerTimeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prayer_time, parent, false)
        return PrayerTimeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerTimeViewHolder, position: Int) {
        holder.bind(prayers[position])
    }

    override fun getItemCount() = prayers.size

    class PrayerTimeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.prayerNameText)
        private val timeText: TextView = itemView.findViewById(R.id.prayerTimeText)

        fun bind(prayer: PrayerTime) {
            nameText.text = prayer.name
            timeText.text = prayer.time
        }
    }
}