package edu.cuhk.csci3310.csci3310project.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import edu.cuhk.csci3310.csci3310project.alarm.storage.Alarm
import edu.cuhk.csci3310.csci3310project.alarm.storage.RepeatType
import java.util.Calendar

object AlarmManager {
    private var requestCode = 0

    @RequiresApi(Build.VERSION_CODES.S)
    fun setAlarm(context: Context, calendar: Calendar, alarm: Alarm) {
        Log.d("AlarmManager", "尝试设置闹钟: ${calendar.time}, ID: ${alarm.id}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AlarmManager", "无法设置精确闹钟，缺少权限")
                return
            }
            
            // 检查是否应该触发
            if (!shouldTriggerToday(alarm, calendar)) {
                Log.d("AlarmManager", "闹钟今天不应该触发")
                return
            }
            
            // 计算下一个触发时间
            val nextTriggerTime = calculateNextTriggerTime(calendar, alarm)
            if (nextTriggerTime == null) {
                Log.d("AlarmManager", "闹钟不需要触发")
                return
            }
            
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", alarm.id)
                putExtra("alarm_label", alarm.label)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context, 
                alarm.id.toInt(),
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(nextTriggerTime.timeInMillis, null),
                pendingIntent
            )
            
            Log.d("AlarmManager", "成功设置闹钟，触发时间: ${nextTriggerTime.time}")
        } catch (e: SecurityException) {
            Log.e("AlarmManager", "设置闹钟失败: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("AlarmManager", "设置闹钟失败: ${e.message}", e)
        }
    }

    private fun calculateNextTriggerTime(calendar: Calendar, alarm: Alarm): Calendar? {
        val now = Calendar.getInstance()
        val targetTime = calendar.clone() as Calendar
        
        // 如果目标时间已经过去，根据重复类型计算下一个触发时间
        if (targetTime.before(now)) {
            when (alarm.repeatType) {
                RepeatType.ONCE -> return null // 单次闹钟，时间已过就不触发
                RepeatType.DAILY -> {
                    targetTime.add(Calendar.DAY_OF_YEAR, 1)
                }
                RepeatType.WEEKDAYS -> {
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (targetTime.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || 
                            targetTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
                }
                RepeatType.WEEKENDS -> {
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (targetTime.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY && 
                            targetTime.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
                }
                RepeatType.WEEKLY -> {
                    val customDays = alarm.customDays?.split(",")?.map { it.toInt() } ?: return null
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (!customDays.contains(targetTime.get(Calendar.DAY_OF_WEEK)))
                }
                RepeatType.MONTHLY -> {
                    targetTime.add(Calendar.MONTH, 1)
                }
                RepeatType.YEARLY -> {
                    targetTime.add(Calendar.YEAR, 1)
                }
                RepeatType.CUSTOM -> {
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