package edu.cuhk.csci3310.csci3310project.backend.features.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/* AlarmReceiver.kt
 * 用于处理闹钟相关的广播事件，包括接收闹钟触发、停止闹钟等事件。
* */

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_STOP_ALARM = "edu.cuhk.csci3310.csci3310project.STOP_ALARM"
        private const val TAG = "AlarmReceiver"
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
                    val isSubAlarm = intent.getBooleanExtra("is_sub_alarm", false)
                    val alarmLabel = intent.getStringExtra("alarm_label") ?: "闹钟提醒"
                    Log.d(TAG, "闹钟信息 - ID: $alarmId, 是否子闹钟: $isSubAlarm, 标签: $alarmLabel")
                    
                    // 启动前台服务
                    startForegroundService(context, alarmId, alarmLabel, isSubAlarm)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理闹钟广播时发生错误: ${e.message}", e)
        }
    }

    // 创建通知渠道
    private fun createNotificationChannel(context: Context) {
        try {
            val name = "闹钟通知"
            val descriptionText = "显示闹钟提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true)
                setSound(null, null)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "通知渠道创建成功")
        } catch (e: Exception) {
            Log.e(TAG, "创建通知渠道失败: ${e.message}", e)
        }
    }

    // 启动前台服务
    private fun startForegroundService(context: Context, alarmId: Long, alarmLabel: String, isSubAlarm: Boolean = false) {
        Log.d(TAG, "准备启动前台服务")
        
        try {
            // 检查通知权限
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "没有通知权限")
                return
            }

            // 创建通知
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("alarm_id", alarmId)
                putExtra("alarm_label", alarmLabel)
                putExtra("is_sub_alarm", isSubAlarm)
                putExtra("send_broadcast", true)
            }

            context.startForegroundService(serviceIntent)
            Log.d(TAG, "前台服务启动成功")
        } catch (e: Exception) {
            Log.e(TAG, "启动前台服务失败: ${e.message}", e)
        }
    }

    // 停止闹钟
    private fun stopAlarm(context: Context) {
        try {
            Log.d(TAG, "准备停止闹钟")
            
            // 取消通知
            with(NotificationManagerCompat.from(context)) {
                cancelAll()
                Log.d(TAG, "通知已取消")
            }
            
            // 停止服务
            val serviceIntent = Intent(context, AlarmService::class.java)
            context.stopService(serviceIntent)
            Log.d(TAG, "服务已停止")
        } catch (e: Exception) {
            Log.e(TAG, "停止闹钟时发生错误: ${e.message}", e)
        }
    }
}