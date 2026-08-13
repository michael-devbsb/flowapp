package com.example.flowwidget

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RoutineAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-agendar todos os alarmes
            rescheduleAllAlarms(context)
            return
        }

        val blockId = intent.getIntExtra("BLOCK_ID", -1)
        val blockName = intent.getStringExtra("BLOCK_NAME") ?: "Tarefa"
        val blockTime = intent.getStringExtra("BLOCK_TIME") ?: ""

        showNotification(context, blockId, blockName, blockTime)
    }

    private fun showNotification(context: Context, id: Int, name: String, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "routine_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Lembretes de Rotina",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações enviadas 15 minutos antes de uma tarefa começar."
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(context, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Próxima Tarefa em 15 min")
            .setContentText("A tarefa '$name' começará às $time.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(id, notification)
    }

    private fun rescheduleAllAlarms(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            val blocks = db.routineDao().getAllBlocks()
            blocks.forEach { block ->
                NotificationScheduler.scheduleAlarm(context, block)
            }
        }
    }
}
