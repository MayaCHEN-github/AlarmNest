package edu.cuhk.csci3310.csci3310project.backend.features.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.cuhk.csci3310.csci3310project.backend.data.Alarm
import edu.cuhk.csci3310.csci3310project.backend.data.RepeatType
import java.util.Calendar

/* AlarmManager.kt:
 * 该类用于设置和管理闹钟，包括设置闹钟、计算下一个触发时间等功能。
* */

object AlarmManager {
    private const val TAG = "AlarmManager"

    private var requestCode = 0

    fun setAlarm(context: Context, calendar: Calendar, alarm: Alarm) {
        Log.d(TAG, "尝试设置闹钟: ${calendar.time}, ID: ${alarm.id}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            // 检查精确闹钟权限
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "无法设置精确闹钟，缺少权限")
                // 检查是否已经请求过权限
                val hasRequestedPermission = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                    .getBoolean("has_requested_exact_alarm", false)
                
                if (!hasRequestedPermission) {
                    Log.d(TAG, "首次请求精确闹钟权限")
                    // 保存已请求权限的状态
                    context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("has_requested_exact_alarm", true)
                        .apply()
                    
                    // 打开系统设置页面去请求权限！
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                return
            }
            
            // 计算下一个触发时间
            val nextTriggerTime = calculateNextTriggerTime(calendar, alarm)
            if (nextTriggerTime == null) {
                Log.d(TAG, "闹钟不需要触发")
                return
            }
            
            Log.d(TAG, "计算得到的下一个触发时间: ${nextTriggerTime.time}")
            Log.d(TAG, "距离触发还有: ${(nextTriggerTime.timeInMillis - System.currentTimeMillis()) / 1000 / 60}分钟")
            
            val intent = Intent(context, AlarmReceiver::class.java).apply { // 设置闹钟触发时的广播接收器
                putExtra("alarm_id", alarm.id)
                putExtra("alarm_label", alarm.label)
                putExtra("alarm_hour", alarm.hour)
                putExtra("alarm_minute", alarm.minute)
                putExtra("alarm_repeat_type", alarm.repeatType.name)
                putExtra("alarm_custom_days", alarm.customDays)
                putExtra("alarm_dismiss_type", alarm.dismissType.name)
                putExtra("alarm_trigger_type", alarm.triggerType.name)
                putExtra("alarm_is_enabled", alarm.isEnabled)
            }
            
            val pendingIntent = PendingIntent.getBroadcast( // 创建PendingIntent
                context, 
                alarm.id.toInt(),
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (pendingIntent == null) {
                Log.e(TAG, "创建PendingIntent失败")
                return
            }

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(nextTriggerTime.timeInMillis, null),
                pendingIntent
            )
            
            Log.d(TAG, "成功设置闹钟，触发时间: ${nextTriggerTime.time}")
        } catch (e: SecurityException) {
            Log.e(TAG, "设置闹钟失败: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "设置闹钟失败: ${e.message}", e)
        }
    }

    fun calculateNextTriggerTime(calendar: Calendar, alarm: Alarm): Calendar? {
        val now = Calendar.getInstance()
        val targetTime = calendar.clone() as Calendar
        
        // 如果目标时间已经过去，根据重复类型计算下一个触发时间
        if (targetTime.before(now)) {
            when (alarm.repeatType) {
                RepeatType.ONCE -> return null // once单次闹钟，时间已过就不触发
                RepeatType.DAILY -> { // daily触发，下一次触发时间为直接加一天
                    targetTime.add(Calendar.DAY_OF_YEAR, 1)
                }
                RepeatType.WEEKDAYS -> { // weekdays触发，下一个触发时间为下一个工作日
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (targetTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                            targetTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                }
                RepeatType.WEEKENDS -> { // weekends触发，下一个触发时间为下一个周末
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (targetTime.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && 
                            targetTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
                }
                RepeatType.WEEKLY -> { // weekly触发，下一个触发时间为下一个指定的星期几
                    val customDays = alarm.customDays?.split(",")?.map { it.toInt() } ?: return null
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (!customDays.contains(targetTime.get(Calendar.DAY_OF_WEEK)))
                }
                RepeatType.MONTHLY -> { // monthly触发，下一个触发时间为下一个指定的日期(月份+1)
                    targetTime.add(Calendar.MONTH, 1)
                }
                RepeatType.YEARLY -> { // yearly触发，下一个触发时间为下一个指定的日期(年份+1)
                    targetTime.add(Calendar.YEAR, 1)
                }
                RepeatType.CUSTOM -> { // custom触发，下一个触发时间为下一个指定的星期几
                    val customDays = alarm.customDays?.split(",")?.map { it.toInt() } ?: return null
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (!customDays.contains(targetTime.get(Calendar.DAY_OF_WEEK)))
                }
            }
        }
        
        return targetTime
    }

    private fun shouldTriggerToday(alarm: Alarm, calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH 从0开始

        return when (alarm.repeatType) {
            RepeatType.ONCE -> true
            RepeatType.DAILY -> true
            RepeatType.WEEKDAYS -> dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
            RepeatType.WEEKENDS -> dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
            RepeatType.WEEKLY -> {
                val customDays = alarm.customDays?.split(",")?.map { it.toInt() } ?: emptyList()
                dayOfWeek in customDays
            }
            RepeatType.MONTHLY -> alarm.dayOfMonth == dayOfMonth
            RepeatType.YEARLY -> alarm.month == month && alarm.dayOfMonth == dayOfMonth
            RepeatType.CUSTOM -> {
                val customDays = alarm.customDays?.split(",")?.map { it.toInt() } ?: emptyList()
                dayOfWeek in customDays
            }
        }
    }
}