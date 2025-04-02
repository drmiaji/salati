package com.salati.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.widget.Toast
import com.salati.R

class MuteAzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == "com.salati.ACTION_MUTE_AZAN") {
            // Stop the MediaPlayer if it is playing
            val mediaPlayer = MediaPlayer.create(context, R.raw.azan_all) // Replace with your actual MediaPlayer instance
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
                Toast.makeText(context, "Azan is muted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}