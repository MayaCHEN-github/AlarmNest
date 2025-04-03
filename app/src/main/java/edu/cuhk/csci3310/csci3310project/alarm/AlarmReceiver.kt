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
            
            createNotificationChannel(context)
            showNotification(context)
            
            Log.d("AlarmReceiver", "通知显示完成")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "闹钟触发失败: ${e.message}", e)
        }
    }

    private fun createNotificationChannel(context: Context) {
        val name = "闹钟通知"
        val descriptionText = "显示闹钟提醒"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableVibration(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(context: Context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w("AlarmReceiver", "没有通知权限，无法显示通知")
            return
        }

        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("闹钟提醒")
                .setContentText("到设定的时间了！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(0, 1000, 500, 1000))

            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "显示通知失败: ${e.message}", e)
        }
    }
}