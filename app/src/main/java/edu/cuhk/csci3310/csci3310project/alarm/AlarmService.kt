package edu.cuhk.csci3310.csci3310project.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import edu.cuhk.csci3310.csci3310project.MainActivity
import edu.cuhk.csci3310.csci3310project.R
import java.util.*

class AlarmService : Service() {
    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
        private const val WAKE_LOCK_TAG = "AlarmService::WakeLock"
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_INTERVAL = 2000L // 2秒
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var currentIntent: Intent? = null
    private var notificationTimer: Timer? = null
    private var isRunning = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "服务已创建")
        
        try {
            // 创建通知渠道
            createNotificationChannel()
            
            // 获取 WakeLock
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            )
            Log.d(TAG, "WakeLock初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "服务创建过程中发生错误: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "服务开始启动，flags: $flags, startId: $startId")
        currentIntent = intent
        
        // 检查是否是停止闹钟的广播
        if (intent?.action == AlarmReceiver.ACTION_STOP_ALARM) {
            Log.d(TAG, "收到停止闹钟的广播，准备停止服务")
            stopSelf()
            return START_NOT_STICKY
        }
        
        if (!isRunning) {
            isRunning = true
            
            try {
                // 创建一个最基本的通知
                val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("闹钟服务")
                    .setContentText("闹钟服务正在运行")
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
                
                // 立即启动前台服务
                startForeground(NOTIFICATION_ID, notification)
                Log.d(TAG, "前台服务已启动")
                
                // 获取 WakeLock
                wakeLock?.let {
                    if (!it.isHeld) {
                        it.acquire(10*60*1000L /*10 minutes*/)
                        Log.d(TAG, "WakeLock已获取")
                    }
                }
                
                // 启动闹钟音乐
                startAlarmSound()
                
                // 启动定时器
                startNotificationTimer()
            } catch (e: Exception) {
                Log.e(TAG, "启动服务时发生错误: ${e.message}", e)
                stopSelf()
                return START_NOT_STICKY
            }
        }
        
        return START_STICKY
    }

    private fun startAlarmSound() {
        try {
            Log.d(TAG, "准备播放闹钟音乐")
            
            // 设置系统音量
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as android.media.AudioManager
            // 设置闹钟音量最大
            audioManager.setStreamVolume(
                android.media.AudioManager.STREAM_ALARM,
                audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM),
                0
            )
            
            // 创建新的MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                try {
                    // 设置音频流类型为闹钟
                    setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    
                    // 设置音量
                    setVolume(1.0f, 1.0f)
                    
                    // 设置数据源
                    setDataSource(this@AlarmService, Uri.parse("android.resource://" + packageName + "/" + R.raw.alarm_sound))
                    
                    // 设置循环播放
                    isLooping = true
                    
                    // 准备播放
                    prepare()
                    
                    // 开始播放
                    start()
                    
                    Log.d(TAG, "闹钟音乐开始播放")
                } catch (e: Exception) {
                    Log.e(TAG, "播放闹钟音乐失败: ${e.message}", e)
                    e.printStackTrace()
                    release()
                    mediaPlayer = null
                    android.os.Handler(mainLooper).postDelayed({
                        startAlarmSound()
                    }, 1000)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "创建MediaPlayer失败: ${e.message}", e)
            e.printStackTrace()
            android.os.Handler(mainLooper).postDelayed({
                startAlarmSound()
            }, 1000)
        }
    }

    private fun stopAlarmSound() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    Log.d(TAG, "音乐已停止")
                }
                release()
            }
            mediaPlayer = null
        } catch (e: Exception) {
            Log.e(TAG, "停止闹钟音乐时发生错误: ${e.message}", e)
        }
    }

    private fun startNotificationTimer() {
        notificationTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    try {
                        val fullNotification = createNotification()
                        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.notify(NOTIFICATION_ID, fullNotification)
                    } catch (e: Exception) {
                        Log.e(TAG, "发送通知失败: ${e.message}", e)
                    }
                }
            }, 0, NOTIFICATION_INTERVAL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "服务已销毁")
        
        isRunning = false
        notificationTimer?.cancel()
        notificationTimer = null
        
        // 停止闹钟音乐
        stopAlarmSound()
        
        try {
            // 释放 WakeLock
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                    Log.d(TAG, "WakeLock已释放")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "释放WakeLock时发生错误: ${e.message}", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val name = "闹钟通知"
                val descriptionText = "显示闹钟提醒"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    setShowBadge(true)
                    setBypassDnd(true)
                    // 禁用通知声音
                    setSound(null, null)
                }

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "通知渠道创建成功")
            } catch (e: Exception) {
                Log.e(TAG, "创建通知渠道失败: ${e.message}", e)
            }
        }
    }

    private fun createNotification(): Notification {
        try {
            val alarmId = currentIntent?.getLongExtra("alarm_id", -1) ?: -1
            val isSubAlarm = currentIntent?.getBooleanExtra("is_sub_alarm", false) ?: false
            val alarmLabel = currentIntent?.getStringExtra("alarm_label") ?: "闹钟提醒"
            val alarmHour = currentIntent?.getIntExtra("alarm_hour", 0) ?: 0
            val alarmMinute = currentIntent?.getIntExtra("alarm_minute", 0) ?: 0
            val alarmRepeatType = currentIntent?.getStringExtra("alarm_repeat_type") ?: "ONCE"
            val alarmCustomDays = currentIntent?.getStringExtra("alarm_custom_days")
            val alarmDismissType = currentIntent?.getStringExtra("alarm_dismiss_type") ?: "NO_ALARM"
            val alarmTriggerType = currentIntent?.getStringExtra("alarm_trigger_type") ?: "TIME"
            val alarmIsEnabled = currentIntent?.getBooleanExtra("alarm_is_enabled", true) ?: true
            
            // 创建打开应用的Intent
            val openAppIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("navigate_to", "alarm_screen")
                putExtra("alarm_id", alarmId)
                putExtra("is_sub_alarm", isSubAlarm)
                putExtra("alarm_label", alarmLabel)
                putExtra("alarm_hour", alarmHour)
                putExtra("alarm_minute", alarmMinute)
                putExtra("alarm_repeat_type", alarmRepeatType)
                putExtra("alarm_custom_days", alarmCustomDays)
                putExtra("alarm_dismiss_type", alarmDismissType)
                putExtra("alarm_trigger_type", alarmTriggerType)
                putExtra("alarm_is_enabled", alarmIsEnabled)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                this,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 发送广播来触发导航
            val broadcastIntent = Intent("edu.cuhk.csci3310.csci3310project.NAVIGATE_TO_ALARM").apply {
                putExtra("alarm_id", alarmId)
                putExtra("is_sub_alarm", isSubAlarm)
                putExtra("alarm_label", alarmLabel)
            }
            sendBroadcast(broadcastIntent)

            return NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(if (isSubAlarm) "子闹钟: $alarmLabel" else alarmLabel)
                .setContentText("到设定的时间了！")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(false)
                .setOngoing(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setContentIntent(openAppPendingIntent)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "创建通知失败: ${e.message}", e)
            throw e
        }
    }
} 
