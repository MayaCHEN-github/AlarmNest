package edu.cuhk.csci3310.csci3310project.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object AlarmManager {
    private var requestCode = 0

    fun setAlarm(context: Context, calendar: Calendar) {
        Log.d("AlarmManager", "尝试设置闹钟: ${calendar.time}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmManager", "无法设置精确闹钟，缺少权限")
                return
            }
            
            val intent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, requestCode++, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            Log.d("AlarmManager", "成功设置闹钟，触发时间: ${calendar.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmManager", "设置闹钟失败: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("AlarmManager", "设置闹钟失败: ${e.message}", e)
        }
    }
}