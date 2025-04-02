package com.salati.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.salati.R
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.TextView

class IslamicCalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_islamic_calendar)

        val islamicDateTextView: TextView = findViewById(R.id.islamicDateTextView)
        val islamicCalendar = UmmalquraCalendar()
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val islamicDate = sdf.format(islamicCalendar.time)

        islamicDateTextView.text = islamicDate
    }
}