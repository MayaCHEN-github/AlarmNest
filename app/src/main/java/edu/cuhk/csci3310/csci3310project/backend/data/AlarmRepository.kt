package edu.cuhk.csci3310.csci3310project.backend.data

import kotlinx.coroutines.flow.Flow
import java.util.*

/* AlarmRepository.kt
 * 这个文件定义了AlarmRepository类，负责封装对数据库的操作.
 * 以及处理数据转换和创建。
 * */

class AlarmRepository(
    private val alarmDao: AlarmDao,      // 主闹钟的数据访问对象
    private val subAlarmDao: SubAlarmDao // 子闹钟的数据访问对象
) {
    // 获取所有闹钟的Flow
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    // 获取所有启用闹钟的Flow
    val enabledAlarms: Flow<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    // 主闹钟操作
    suspend fun insert(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }
    
    // 更新闹钟
    suspend fun update(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }
    
    // 删除闹钟
    suspend fun delete(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }
    
    // 根据ID获取闹钟(不存在则返回null)
    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)
    }
    
    // 根据重复类型获取闹钟
    fun getAlarmsByRepeatType(repeatType: RepeatType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByRepeatType(repeatType)
    }
    
    // 根据触发方式获取闹钟
    fun getAlarmsByTriggerType(triggerType: TriggerType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByTriggerType(triggerType)
    }
    
    // 根据关闭方式获取闹钟
    fun getAlarmsByDismissType(dismissType: DismissType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByDismissType(dismissType)
    }
    
    // 获取相应parentId的所有子闹钟
    fun getSubAlarmsByParentId(parentAlarmId: Long): Flow<List<SubAlarm>> {
        return subAlarmDao.getSubAlarmsByParentId(parentAlarmId)
    }
    
    // 插入新的子闹钟
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long {
        return subAlarmDao.insertSubAlarm(subAlarm)
    }
    
    // 更新子闹钟
    suspend fun updateSubAlarm(subAlarm: SubAlarm) {
        subAlarmDao.updateSubAlarm(subAlarm)
    }
    
    // 删除子闹钟
    suspend fun deleteSubAlarm(subAlarm: SubAlarm) {
        subAlarmDao.deleteSubAlarm(subAlarm)
    }
    
    // 根据ID获取子闹钟(不存在则返回null)
    suspend fun getSubAlarmById(id: Long): SubAlarm? {
        return subAlarmDao.getSubAlarmById(id)
    }
    
    // 删除指定主闹钟的所有子闹钟
    suspend fun deleteAllSubAlarmsByParentId(parentAlarmId: Long) {
        subAlarmDao.deleteAllSubAlarmsByParentId(parentAlarmId)
    }
    
    // 数据转换和创建
    fun createAlarmFromCalendar(
        calendar: Calendar, // 包含时间的Calendar对象
        repeatType: RepeatType = RepeatType.ONCE, // 重复类型
        customDays: String? = null, // 自定义天数
        dismissType: DismissType = DismissType.NO_ALARM, // 关闭方式
        triggerType: TriggerType = TriggerType.TIME, // 触发方式
        label: String = "", // 备注
        customIntervalDays: Int = 0 // 自定义间隔天数
    ): Alarm {
        return Alarm(
            hour = calendar.get(Calendar.HOUR_OF_DAY), // 小时
            minute = calendar.get(Calendar.MINUTE), // 分钟
            repeatType = repeatType, // 重复类型
            customDays = customDays, // 自定义天数
            dismissType = dismissType, // 关闭方式
            triggerType = triggerType, // 触发方式
            label = label, // 备注
            customIntervalDays = customIntervalDays, // 自定义间隔天数
            dayOfMonth = if (repeatType == RepeatType.MONTHLY || repeatType == RepeatType.YEARLY) {
                calendar.get(Calendar.DAY_OF_MONTH) // 如果是每月或每年重复，则获取日期
            } else null,
            month = if (repeatType == RepeatType.YEARLY) {
                calendar.get(Calendar.MONTH) + 1 // 如果是每年重复，则获取月份（月份从0开始!为什么我也没弄清楚。）
            } else null
        )
    }
    
    // 从Alarm实体创建Calendar对象
    fun createCalendarFromAlarm(alarm: Alarm): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }
    }
    
    // 创建子闹钟
    fun createSubAlarm(
        parentAlarmId: Long, // 主闹钟ID
        timeOffsetMinutes: Int, // 时间偏移（分钟）
        dismissType: DismissType = DismissType.NO_ALARM, // 关闭方式
        label: String = "", // 备注
        absoluteTriggerTime: Long? = null // 绝对触发时间
    ): SubAlarm {
        return SubAlarm(
            parentAlarmId = parentAlarmId,
            timeOffsetMinutes = timeOffsetMinutes,
            dismissType = dismissType,
            label = label,
            absoluteTriggerTime = absoluteTriggerTime
        )
    }
} 