package com.example.flowwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.flowwidget.data.RoutineRepository
import com.example.flowwidget.data.local.RoutineBlock
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RoutineAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: RoutineRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllAlarms(context)
            return
        }

        val blockId = intent.getIntExtra("BLOCK_ID", -1)
        val blockName = intent.getStringExtra("BLOCK_NAME") ?: "Tarefa"
        val blockTime = intent.getStringExtra("BLOCK_TIME") ?: ""
        val reminderMinutes = intent.getIntExtra("REMINDER_MINUTES", 15)

        showNotification(context, blockId, blockName, blockTime, reminderMinutes)
        
        // Reagendamento automático para rotinas fixas
        if (blockId != -1) {
            rescheduleNextOccurrence(context, blockId)
        }
    }

    private fun rescheduleNextOccurrence(context: Context, blockId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val blocks = repository.getAllBlocks()
            val block = blocks.find { it.id == blockId }
            if (block != null && block.isFixed) {
                NotificationScheduler.scheduleAlarm(context, block)
            }
        }
    }

    private fun showNotification(context: Context, id: Int, name: String, time: String, reminder: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "routine_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Rotina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações enviadas antes de uma tarefa começar."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("navigate_to", "settings")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Próxima Tarefa em $reminder min")
            .setContentText("A tarefa '$name' começará às $time.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val blocks = repository.getAllBlocks()
            blocks.forEach { block ->
                NotificationScheduler.scheduleAlarm(context, block)
            }
        }
    }
}
