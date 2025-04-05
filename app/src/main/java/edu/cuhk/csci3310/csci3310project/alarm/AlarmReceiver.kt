package edu.cuhk.csci3310.csci3310project.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import edu.cuhk.csci3310.csci3310project.MainActivity
import edu.cuhk.csci3310.csci3310project.R

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_STOP_ALARM = "edu.cuhk.csci3310.csci3310project.STOP_ALARM"
        private const val TAG = "AlarmReceiver"
        private var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "收到广播，action: ${intent.action}")
        
        try {
            when (intent.action) {
                ACTION_STOP_ALARM -> {
                    Log.d(TAG, "收到停止闹钟的广播")
                    stopAlarm(context)
                    return
                }
                else -> {
                    Log.d(TAG, "收到闹钟触发广播")
                    // 确保通知渠道已创建
                    createNotificationChannel(context)
                    
                    // 获取闹钟信息
                    val alarmId = intent.getLongExtra("alarm_id", -1)
                    val alarmLabel = intent.getStringExtra("alarm_label") ?: "闹钟提醒"
                    Log.d(TAG, "闹钟信息 - ID: $alarmId, 标签: $alarmLabel")
                    
                    // 启动前台服务
                    startForegroundService(context, alarmId, alarmLabel)
                    
                    // 播放闹钟音乐
                    playAlarmSound(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理闹钟广播时发生错误: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "闹钟通知"
                val descriptionText = "显示闹钟提醒"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableVibration(true)
                    setShowBadge(true)
                    setBypassDnd(true)
                }

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
                Log.d(TAG, "通知渠道创建成功")
            }
        } catch (e: Exception) {
            Log.e(TAG, "创建通知渠道失败: ${e.message}", e)
        }
    }

    private fun startForegroundService(context: Context, alarmId: Long, alarmLabel: String) {
        Log.d(TAG, "准备启动前台服务")
        
        try {
            // 检查通知权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "没有通知权限")
                return
            }

            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", alarmLabel)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d(TAG, "前台服务启动成功")
        } catch (e: Exception) {
            Log.e(TAG, "启动前台服务失败: ${e.message}", e)
        }
    }

    private fun playAlarmSound(context: Context) {
        try {
            Log.d(TAG, "准备播放闹钟音乐")
            
            // 停止之前的播放
            stopAlarm(context)
            
            // 创建新的MediaPlayer
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + R.raw.alarm_sound))
                isLooping = true
                prepare()
                start()
            }
            Log.d(TAG, "闹钟音乐开始播放")
        } catch (e: Exception) {
            Log.e(TAG, "播放闹钟音乐失败: ${e.message}", e)
        }
    }

    private fun stopAlarm(context: Context) {
        try {
            Log.d(TAG, "准备停止闹钟")
            
            // 停止音乐播放
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                    Log.d(TAG, "音乐已停止")
                }
                release()
            }
            mediaPlayer = null
            
            // 取消通知
            with(NotificationManagerCompat.from(context)) {
                cancelAll()
                Log.d(TAG, "通知已取消")
            }
        } catch (e: Exception) {
            Log.e(TAG, "停止闹钟时发生错误: ${e.message}", e)
        }
    }
}