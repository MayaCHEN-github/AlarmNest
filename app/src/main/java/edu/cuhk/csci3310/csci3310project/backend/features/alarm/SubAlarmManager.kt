package edu.cuhk.csci3310.csci3310project.backend.features.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import edu.cuhk.csci3310.csci3310project.backend.data.Alarm
import edu.cuhk.csci3310.csci3310project.backend.data.RepeatType
import edu.cuhk.csci3310.csci3310project.backend.data.SubAlarm
import java.util.Calendar

/* SubAlarmManager.kt
 * 用于处理子闹钟相关的管理，包括设置子闹钟、计算子闹钟触发时间等功能。
 * 应该是类似于主闹钟的功能，主要是我不敢乱动主闹钟的代码所以重写了一个……
* */

object SubAlarmManager {
    private const val TAG = "SubAlarmManager"

    fun setSubAlarm(context: Context, subAlarm: SubAlarm, parentAlarm: Alarm) {
        Log.d(TAG, "尝试设置子闹钟: ID=${subAlarm.id}, parentID=${parentAlarm.id}")
        Log.d(TAG, "子闹钟详情: timeOffset=${subAlarm.timeOffsetMinutes}分钟, label=${subAlarm.label}")
        Log.d(TAG, "父闹钟详情: time=${parentAlarm.hour}:${parentAlarm.minute}, repeatType=${parentAlarm.repeatType}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        try {
            // 检查是否有设置精确闹钟的权限
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w(TAG, "无法设置精确闹钟，缺少权限")
                return
            }

            // 计算子闹钟的绝对触发时间
            val triggerTime = calculateSubAlarmTriggerTime(subAlarm, parentAlarm)
            if (triggerTime == null) {
                Log.d(TAG, "子闹钟不需要触发")
                return
            }

            // 设置子闹钟
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("alarm_id", subAlarm.id)
                putExtra("is_sub_alarm", true)
                putExtra("alarm_label", subAlarm.label)
            }
            
            val pendingIntent = PendingIntent.getBroadcast( // 创建PendingIntent
                context,
                -subAlarm.id.toInt(), // 使用负数ID避免与主闹钟ID冲突!
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setAlarmClock( // 设置闹钟
                AlarmManager.AlarmClockInfo(triggerTime.timeInMillis, null),
                pendingIntent
            )
            
            Log.d(TAG, "成功设置子闹钟，触发时间: ${triggerTime.time}")
            Log.d(TAG, "距离触发还有: ${(triggerTime.timeInMillis - System.currentTimeMillis()) / 1000 / 60}分钟")
        } catch (e: SecurityException) {
            Log.e(TAG, "设置子闹钟失败: ${e.message}", e)
        } catch (e: Exception) {
            Log.e(TAG, "设置子闹钟失败: ${e.message}", e)
        }
    }

    // 计算子闹钟的触发时间
    fun calculateSubAlarmTriggerTime(subAlarm: SubAlarm, parentAlarm: Alarm): Calendar? {
        Log.d(TAG, "开始计算子闹钟触发时间")
        Log.d(TAG, "父闹钟时间: ${parentAlarm.hour}:${parentAlarm.minute}")
        Log.d(TAG, "父闹钟重复类型: ${parentAlarm.repeatType}")
        Log.d(TAG, "子闹钟时间偏移: ${subAlarm.timeOffsetMinutes}分钟")
        
        // 获取当前时间
        val now = Calendar.getInstance()
        Log.d(TAG, "当前时间: ${now.time}")
        
        // 获取今天的基准时间（父闹钟的时间）
        val baseTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parentAlarm.hour)
            set(Calendar.MINUTE, parentAlarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Log.d(TAG, "今天的基准时间: ${baseTime.time}")

        // 计算子闹钟的目标时间
        val targetTime = Calendar.getInstance().apply {
            timeInMillis = baseTime.timeInMillis
            add(Calendar.MINUTE, subAlarm.timeOffsetMinutes)
        }
        Log.d(TAG, "计算得到目标时间: ${targetTime.time}")
        
        // 如果目标时间已过，需要移到下一个周期
        if (targetTime.before(now)) {
            Log.d(TAG, "目标时间已过，移到下一个周期")
            when (parentAlarm.repeatType) { // 这里就不写了……
                RepeatType.ONCE -> {
                    Log.d(TAG, "单次闹钟已过期")
                    return null
                }
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
                    val customDays = parentAlarm.customDays?.split(",")?.map { it.toInt() } ?: return null
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
                    val customDays = parentAlarm.customDays?.split(",")?.map { it.toInt() } ?: return null
                    do {
                        targetTime.add(Calendar.DAY_OF_YEAR, 1)
                    } while (!customDays.contains(targetTime.get(Calendar.DAY_OF_WEEK)))
                }
            }
            Log.d(TAG, "移到下一个周期后的时间: ${targetTime.time}")
        }
        
        Log.d(TAG, "最终的子闹钟触发时间: ${targetTime.time}")
        return targetTime
    }

    // 计算子闹钟的绝对触发时间（但是因为太过buggy最后好像没用上……！）
    fun calculateAbsoluteTriggerTime(subAlarm: SubAlarm, parentAlarm: Alarm): Long? {
        return calculateSubAlarmTriggerTime(subAlarm, parentAlarm)?.timeInMillis
    }
} 