package com.example.flowwidget

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class FloatingWidgetService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null
    private var isWidgetVisible = false
    private var isManuallyHidden = false
    private var isDestroyedByUser = false
    
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    private lateinit var dao: RoutineDao
    private lateinit var layoutParams: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra("STOP_BY_USER", false) == true) {
            isDestroyedByUser = true
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT else PendingIntent.FLAG_ONE_SHOT
        )
        
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        alarmManager.set(android.app.AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, restartServicePendingIntent)

        super.onTaskRemoved(rootIntent)
    }

    override fun onCreate() {
        super.onCreate()

        startForegroundServiceCompat()

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        dao = AppDatabase.getDatabase(this).routineDao()

        // Inflar o layout uma vez
        floatingView = LayoutInflater.from(this).inflate(R.layout.widget_layout, null)

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE

        val screenWidth = resources.displayMetrics.widthPixels

        layoutParams = WindowManager.LayoutParams(
            screenWidth / 4,//WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 100
        layoutParams.y = 100

        // Inicialmente adicionamos o widget
        showWidget()
        setupDragging()
        
        floatingView?.findViewById<Button>(R.id.btn_ocultar)?.setOnClickListener {
            isDestroyedByUser = true
            stopSelf()
        }

        startUpdateLoop()
    }

    private fun showWidget() {
        if (!isWidgetVisible && floatingView != null) {
            windowManager.addView(floatingView, layoutParams)
            isWidgetVisible = true
        }
    }

    private fun hideWidget() {
        if (isWidgetVisible && floatingView != null) {
            windowManager.removeView(floatingView)
            isWidgetVisible = false
        }
    }

    private fun setupDragging() {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.0f
            private var initialTouchY: Float = 0.0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        if (isWidgetVisible) {
                            windowManager.updateViewLayout(floatingView, layoutParams)
                        }
                        return true
                    }
                }
                return false
            }
        })
    }

    private fun startUpdateLoop() {
        serviceScope.launch {
            var lastBlockId: Int? = -1
            
            while (isActive) {
                val now = Calendar.getInstance()
                val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now.time)
                val dayOfWeek = now.get(Calendar.DAY_OF_WEEK)
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now.time)
                
                val activeBlock = withContext(Dispatchers.IO) {
                    dao.getActiveBlock(currentTime, dayOfWeek, dateStr)
                }

                // Lógica de auto-exibição
                if (activeBlock?.id != lastBlockId) {
                    // Se mudou o bloco, resetamos o "ocultar manual" se for um bloco de rotina real
                    if (activeBlock != null) {
                        isManuallyHidden = false
                    }
                    lastBlockId = activeBlock?.id
                }

                if (activeBlock != null) {
                    // Se houver uma tarefa ativa, ocultamos o botão de ocultar e garantimos que o widget esteja visível
                    floatingView?.findViewById<Button>(R.id.btn_ocultar)?.visibility = View.GONE
                    showWidget()
                } else {
                    // Sem bloco: Modo descanso. Mostramos o botão de ocultar.
                    floatingView?.findViewById<Button>(R.id.btn_ocultar)?.visibility = View.VISIBLE
                    
                    // Se o usuário ocultou manualmente durante o descanso, respeitamos. 
                    // Caso contrário, mantemos visível (ou mostramos se for a transição para descanso).
                    if (isManuallyHidden) {
                        hideWidget()
                    } else {
                        showWidget()
                    }
                }

                updateWidgetUI(activeBlock, now)
                delay(1000)
            }
        }
    }

    private fun updateWidgetUI(block: RoutineBlock?, now: Calendar) {
        if (!isWidgetVisible) return

        val root = floatingView?.findViewById<View>(R.id.widget_root)
        val title = floatingView?.findViewById<TextView>(R.id.txt_tarefa_titulo)
        val list = floatingView?.findViewById<TextView>(R.id.txt_tarefas_lista)
        val timer = floatingView?.findViewById<TextView>(R.id.txt_timer)

        if (block != null) {
            title?.text = "• ${block.name}"
            list?.text = block.tasks
            
            try {
                val color = Color.parseColor(block.colorHex)
                root?.background?.setTint(color)
            } catch (e: Exception) {
                root?.background?.setTint(Color.parseColor("#A6FFFFFF"))
            }

            timer?.text = calculateCountdown(block.endTime, now)
        } else {
            title?.text = "Descanso"
            list?.text = "Nenhum bloco ativo"
            timer?.text = "00:00:00"
            root?.background?.setTint(Color.parseColor("#A6444444"))
        }
    }

    private fun calculateCountdown(endTimeStr: String, now: Calendar): String {
        return try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val endDate = sdf.parse(endTimeStr) ?: return "00:00:00"
            
            val endCalendar = Calendar.getInstance().apply {
                time = endDate
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
            }

            if (endCalendar.before(now)) {
                endCalendar.add(Calendar.DATE, 1)
            }

            val diff = endCalendar.timeInMillis - now.timeInMillis
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60
            val seconds = (diff / 1000) % 60

            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    private fun startForegroundServiceCompat() {
        val channelId = "floating_widget_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "FlowWidget", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("FlowWidget Monitorando")
            .setContentText("Sua rotina está ativa.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        hideWidget()
        if (!isDestroyedByUser) {
            val broadcastIntent = Intent("com.example.flowwidget.RESTART_WIDGET")
            broadcastIntent.setPackage(packageName)
            sendBroadcast(broadcastIntent)
        }
    }
}
