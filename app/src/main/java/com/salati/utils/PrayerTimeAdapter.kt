package com.salati.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit
import com.salati.R

class PrayerTimeAdapter(private var prayers: List<PrayerTime>, private val context: Context) :
    RecyclerView.Adapter<PrayerTimeAdapter.PrayerViewHolder>() {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("com.salati", Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_prayer_time, parent, false)
        return PrayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrayerViewHolder, position: Int) {
        val prayer = prayers[position]
        holder.bind(prayer)
    }

    override fun getItemCount(): Int = prayers.size

    @SuppressLint("NotifyDataSetChanged")
    fun updatePrayers(newPrayers: List<PrayerTime>) {
        prayers = newPrayers
        notifyDataSetChanged()
    }

    inner class PrayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val prayerNameText: TextView = itemView.findViewById(R.id.prayerNameText)
        private val prayerTimeText: TextView = itemView.findViewById(R.id.prayerTimeText)
        private val audioIcon: ImageView = itemView.findViewById(R.id.audioIcon)

        fun bind(prayer: PrayerTime) {
            prayerNameText.text = prayer.name
            prayerTimeText.text = prayer.time

            var isAzanEnabled = sharedPreferences.getBoolean("${prayer.name}_azan", true)
            updateAudioIcon(isAzanEnabled)

            audioIcon.setOnClickListener {
                isAzanEnabled = !isAzanEnabled
                sharedPreferences.edit { putBoolean("${prayer.name}_azan", isAzanEnabled) }
                updateAudioIcon(isAzanEnabled)
                showToast(isAzanEnabled, prayer.name)
            }
        }

        private fun updateAudioIcon(isAzanEnabled: Boolean) {
            if (isAzanEnabled) {
                audioIcon.setImageResource(R.drawable.ic_sound_on)
            } else {
                audioIcon.setImageResource(R.drawable.ic_sound_off)
            }
        }

        private fun showToast(isAzanEnabled: Boolean, prayerName: String) {
            val status = if (isAzanEnabled) "ON" else "OFF"
            Toast.makeText(context, "Azan is $status for $prayerName", Toast.LENGTH_SHORT).show()
        }
    }
}