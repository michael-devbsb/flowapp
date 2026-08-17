package com.example.flowwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.flowwidget.data.local.RoutineBlock
import java.text.SimpleDateFormat
import java.util.*

object NotificationScheduler {

    fun scheduleAlarm(context: Context, block: RoutineBlock) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTriggerTime = calculateNextTriggerTime(block) ?: return

        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            putExtra("BLOCK_ID", block.id)
            putExtra("BLOCK_NAME", block.name)
            putExtra("BLOCK_TIME", block.startTime)
            putExtra("REMINDER_MINUTES", block.reminderMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            block.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            } else {
                // Se não puder alarme exato, usamos o inexato ou pedimos permissão
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTriggerTime,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                nextTriggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, block: RoutineBlock) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, RoutineAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            block.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun calculateNextTriggerTime(block: RoutineBlock): Long? {
        val preAlarmMinutes = block.reminderMinutes

        val now = Calendar.getInstance()
        
        val startTimeCal = Calendar.getInstance()
        val parts = block.startTime.split(":")
        startTimeCal.set(Calendar.HOUR_OF_DAY, parts[0].toInt())
        startTimeCal.set(Calendar.MINUTE, parts[1].toInt())
        startTimeCal.set(Calendar.SECOND, 0)
        startTimeCal.set(Calendar.MILLISECOND, 0)

        // Subtrair minutos configurados
        startTimeCal.add(Calendar.MINUTE, -preAlarmMinutes)

        if (block.isFixed) {
            val days = block.selectedDays?.split(",")?.map { it.toInt() } ?: return null
            
            // Encontrar o próximo dia de disparo
            var daysFound = false
            val searchCal = Calendar.getInstance().apply { time = startTimeCal.time }
            for (i in 0 until 8) {
                val currentDayOfWeek = searchCal.get(Calendar.DAY_OF_WEEK)
                if (days.contains(currentDayOfWeek) && searchCal.after(now)) {
                    startTimeCal.time = searchCal.time
                    daysFound = true
                    break
                }
                searchCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return if (daysFound) startTimeCal.timeInMillis else null
        } else {
            val dateSdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = block.date?.let { dateSdf.parse(it) } ?: return null
            val targetCal = Calendar.getInstance().apply { time = date }
            
            startTimeCal.set(Calendar.YEAR, targetCal.get(Calendar.YEAR))
            startTimeCal.set(Calendar.MONTH, targetCal.get(Calendar.MONTH))
            startTimeCal.set(Calendar.DAY_OF_MONTH, targetCal.get(Calendar.DAY_OF_MONTH))
            
            return if (startTimeCal.after(now)) startTimeCal.timeInMillis else null
        }
    }
}
