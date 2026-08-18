package com.example.flowwidget.ui.models

import java.util.Calendar

data class CalendarDay(
    val id: Long,
    val calendar: Calendar,
    val dayName: String,
    val dayNumber: String,
    val diffDays: Int
)
