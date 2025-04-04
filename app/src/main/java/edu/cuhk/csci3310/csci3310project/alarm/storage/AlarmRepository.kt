package edu.cuhk.csci3310.csci3310project.alarm.storage

import kotlinx.coroutines.flow.Flow
import java.util.*

/**
 * 仓库类
 * 这个类负责：
 * 1. 封装对数据库的所有操作
 * 2. 提供更高级的业务逻辑
 * 3. 处理数据转换和创建
 */
class AlarmRepository(
    private val alarmDao: AlarmDao,      // 主闹钟的数据访问对象
    private val subAlarmDao: SubAlarmDao // 子闹钟的数据访问对象
) {
    // 获取所有闹钟的Flow
    val allAlarms: Flow<List<Alarm>> = alarmDao.getAllAlarms()
    // 获取所有启用闹钟的Flow
    val enabledAlarms: Flow<List<Alarm>> = alarmDao.getEnabledAlarms()
    
    // 主闹钟操作
    /**
     * 插入新闹钟
     * @param alarm 要插入的闹钟
     * @return 新插入闹钟的ID
     */
    suspend fun insert(alarm: Alarm): Long {
        return alarmDao.insertAlarm(alarm)
    }
    
    /**
     * 更新闹钟
     * @param alarm 要更新的闹钟
     */
    suspend fun update(alarm: Alarm) {
        alarmDao.updateAlarm(alarm)
    }
    
    /**
     * 删除闹钟
     * @param alarm 要删除的闹钟
     */
    suspend fun delete(alarm: Alarm) {
        alarmDao.deleteAlarm(alarm)
    }
    
    /**
     * 根据ID获取闹钟
     * @param id 闹钟ID
     * @return 找到的闹钟，如果不存在则返回null
     */
    suspend fun getAlarmById(id: Long): Alarm? {
        return alarmDao.getAlarmById(id)
    }
    
    /**
     * 根据重复类型获取闹钟
     * @param repeatType 重复类型
     * @return 匹配的闹钟列表的Flow
     */
    fun getAlarmsByRepeatType(repeatType: RepeatType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByRepeatType(repeatType)
    }
    
    /**
     * 根据触发类型获取闹钟
     * @param triggerType 触发类型
     * @return 匹配的闹钟列表的Flow
     */
    fun getAlarmsByTriggerType(triggerType: TriggerType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByTriggerType(triggerType)
    }
    
    /**
     * 根据关闭方式获取闹钟
     * @param dismissType 关闭方式
     * @return 匹配的闹钟列表的Flow
     */
    fun getAlarmsByDismissType(dismissType: DismissType): Flow<List<Alarm>> {
        return alarmDao.getAlarmsByDismissType(dismissType)
    }
    
    // 子闹钟操作
    /**
     * 获取指定主闹钟的所有子闹钟
     * @param parentAlarmId 主闹钟ID
     * @return 子闹钟列表的Flow
     */
    fun getSubAlarmsByParentId(parentAlarmId: Long): Flow<List<SubAlarm>> {
        return subAlarmDao.getSubAlarmsByParentId(parentAlarmId)
    }
    
    /**
     * 插入新子闹钟
     * @param subAlarm 要插入的子闹钟
     * @return 新插入子闹钟的ID
     */
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long {
        return subAlarmDao.insertSubAlarm(subAlarm)
    }
    
    /**
     * 更新子闹钟
     * @param subAlarm 要更新的子闹钟
     */
    suspend fun updateSubAlarm(subAlarm: SubAlarm) {
        subAlarmDao.updateSubAlarm(subAlarm)
    }
    
    /**
     * 删除子闹钟
     * @param subAlarm 要删除的子闹钟
     */
    suspend fun deleteSubAlarm(subAlarm: SubAlarm) {
        subAlarmDao.deleteSubAlarm(subAlarm)
    }
    
    /**
     * 根据ID获取子闹钟
     * @param id 子闹钟ID
     * @return 找到的子闹钟，如果不存在则返回null
     */
    suspend fun getSubAlarmById(id: Long): SubAlarm? {
        return subAlarmDao.getSubAlarmById(id)
    }
    
    /**
     * 删除指定主闹钟的所有子闹钟
     * @param parentAlarmId 主闹钟ID
     */
    suspend fun deleteAllSubAlarmsByParentId(parentAlarmId: Long) {
        subAlarmDao.deleteAllSubAlarmsByParentId(parentAlarmId)
    }
    
    // 数据转换和创建方法
    /**
     * 从Calendar创建Alarm实体
     * @param calendar 包含时间的Calendar对象
     * @param repeatType 重复类型
     * @param customDays 自定义天数
     * @param dismissType 关闭方式
     * @param triggerType 触发方式
     * @param label 备注
     * @param customIntervalDays 自定义间隔天数
     * @return 创建的Alarm实体
     */
    fun createAlarmFromCalendar(
        calendar: Calendar,
        repeatType: RepeatType = RepeatType.ONCE,
        customDays: String? = null,
        dismissType: DismissType = DismissType.NO_ALARM,
        triggerType: TriggerType = TriggerType.TIME,
        label: String = "",
        customIntervalDays: Int = 0
    ): Alarm {
        return Alarm(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            minute = calendar.get(Calendar.MINUTE),
            repeatType = repeatType,
            customDays = customDays,
            dismissType = dismissType,
            triggerType = triggerType,
            label = label,
            customIntervalDays = customIntervalDays,
            dayOfMonth = if (repeatType == RepeatType.MONTHLY || repeatType == RepeatType.YEARLY) {
                calendar.get(Calendar.DAY_OF_MONTH)
            } else null,
            month = if (repeatType == RepeatType.YEARLY) {
                calendar.get(Calendar.MONTH) + 1
            } else null
        )
    }
    
    /**
     * 从Alarm实体创建Calendar
     * @param alarm 闹钟实体
     * @return 包含时间的Calendar对象
     */
    fun createCalendarFromAlarm(alarm: Alarm): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }
    }
    
    /**
     * 创建子闹钟
     * @param parentAlarmId 主闹钟ID
     * @param timeOffsetMinutes 时间偏移（分钟）
     * @param dismissType 关闭方式
     * @param label 备注
     * @param absoluteTriggerTime 绝对触发时间
     * @return 创建的SubAlarm实体
     */
    fun createSubAlarm(
        parentAlarmId: Long,
        timeOffsetMinutes: Int,
        dismissType: DismissType = DismissType.NO_ALARM,
        label: String = "",
        absoluteTriggerTime: Long? = null
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