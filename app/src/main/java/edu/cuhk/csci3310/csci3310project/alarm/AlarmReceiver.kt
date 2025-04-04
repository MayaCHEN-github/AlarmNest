package edu.cuhk.csci3310.csci3310project.alarm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "alarm_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent) {
        try {
            Log.d("AlarmReceiver", "闹钟被触发，准备显示通知")
            
            // 确保通知渠道已创建
            createNotificationChannel(context)
            
            // 获取闹钟信息
            val alarmId = intent.getLongExtra("alarm_id", -1)
            val alarmLabel = intent.getStringExtra("alarm_label") ?: "闹钟提醒"
            
            showNotification(context, alarmId, alarmLabel)
            
            Log.d("AlarmReceiver", "通知显示完成")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "闹钟触发失败: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "闹钟通知"
            val descriptionText = "显示闹钟提醒"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                setBypassDnd(true)  // 允许在勿扰模式下显示
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d("AlarmReceiver", "通知渠道创建成功")
        }
    }

    private fun showNotification(context: Context, alarmId: Long, alarmLabel: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w("AlarmReceiver", "没有通知权限，无法显示通知")
            return
        }

        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(alarmLabel)
                .setContentText("到设定的时间了！")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID + alarmId.toInt(), builder.build())
                Log.d("AlarmReceiver", "通知已发送")
            }
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "显示通知失败: ${e.message}", e)
        }
    }
}