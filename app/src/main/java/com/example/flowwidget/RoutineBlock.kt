package com.example.flowwidget

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_blocks")
data class RoutineBlock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startTime: String, // "HH:mm"
    val endTime: String,   // "HH:mm"
    val colorHex: String,
    val tasks: String,      // Lista de tarefas separadas por \n
    val isFixed: Boolean = true,
    val selectedDays: String? = null, // CSV string: "1,3,5" (1=Dom, 7=Sab)
    val date: String? = null    // "yyyy-MM-dd"
)
