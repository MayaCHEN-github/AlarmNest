package edu.cuhk.csci3310.csci3310project.alarm.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 数据访问对象(DAO)接口
 * 这个接口定义了所有对数据库的操作方法
 * 使用注解来标记SQL查询和操作
 */
@Dao
interface AlarmDao {
    /**
     * 获取所有闹钟，按小时和分钟排序
     * @return 闹钟列表的Flow（可以观察数据变化）
     */
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>>
    
    /**
     * 插入新闹钟
     * @param alarm 要插入的闹钟
     * @return 新插入闹钟的ID
     */
    @Insert
    suspend fun insertAlarm(alarm: Alarm): Long
    
    /**
     * 更新闹钟
     * @param alarm 要更新的闹钟
     */
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    /**
     * 删除闹钟
     * @param alarm 要删除的闹钟
     */
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    /**
     * 根据ID获取闹钟
     * @param alarmId 闹钟ID
     * @return 找到的闹钟，如果不存在则返回null
     */
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): Alarm?
    
    /**
     * 获取所有启用的闹钟
     * @return 启用闹钟列表的Flow
     */
    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    fun getEnabledAlarms(): Flow<List<Alarm>>
    
    /**
     * 根据重复类型获取闹钟
     * @param repeatType 重复类型
     * @return 匹配的闹钟列表的Flow
     */
    @Query("SELECT * FROM alarms WHERE repeatType = :repeatType")
    fun getAlarmsByRepeatType(repeatType: RepeatType): Flow<List<Alarm>>
    
    /**
     * 根据触发类型获取闹钟
     * @param triggerType 触发类型
     * @return 匹配的闹钟列表的Flow
     */
    @Query("SELECT * FROM alarms WHERE triggerType = :triggerType")
    fun getAlarmsByTriggerType(triggerType: TriggerType): Flow<List<Alarm>>
    
    /**
     * 根据关闭方式获取闹钟
     * @param dismissType 关闭方式
     * @return 匹配的闹钟列表的Flow
     */
    @Query("SELECT * FROM alarms WHERE dismissType = :dismissType")
    fun getAlarmsByDismissType(dismissType: DismissType): Flow<List<Alarm>>
    
    /**
     * 清理过期的闹钟
     * 删除24小时前触发的闹钟
     * @param thresholdTime 时间阈值，默认为24小时前
     */
    @Query("""
        DELETE FROM alarms 
        WHERE isEnabled = 1 
        AND lastTriggered IS NOT NULL 
        AND lastTriggered < :thresholdTime
    """)
    suspend fun cleanupExpiredAlarms(thresholdTime: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000)
}

/**
 * 子闹钟的数据访问对象(DAO)接口
 * 定义了所有对子闹钟表的操作
 */
@Dao
interface SubAlarmDao {
    /**
     * 获取指定主闹钟的所有子闹钟，按时间偏移排序
     * @param parentAlarmId 主闹钟ID
     * @return 子闹钟列表的Flow
     */
    @Query("SELECT * FROM sub_alarms WHERE parentAlarmId = :parentAlarmId ORDER BY timeOffsetMinutes")
    fun getSubAlarmsByParentId(parentAlarmId: Long): Flow<List<SubAlarm>>
    
    /**
     * 插入新子闹钟
     * @param subAlarm 要插入的子闹钟
     * @return 新插入子闹钟的ID
     */
    @Insert
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long
    
    /**
     * 更新子闹钟
     * @param subAlarm 要更新的子闹钟
     */
    @Update
    suspend fun updateSubAlarm(subAlarm: SubAlarm)
    
    /**
     * 删除子闹钟
     * @param subAlarm 要删除的子闹钟
     */
    @Delete
    suspend fun deleteSubAlarm(subAlarm: SubAlarm)
    
    /**
     * 根据ID获取子闹钟
     * @param subAlarmId 子闹钟ID
     * @return 找到的子闹钟，如果不存在则返回null
     */
    @Query("SELECT * FROM sub_alarms WHERE id = :subAlarmId")
    suspend fun getSubAlarmById(subAlarmId: Long): SubAlarm?
    
    /**
     * 获取所有启用的子闹钟
     * @return 启用的子闹钟列表的Flow
     */
    @Query("SELECT * FROM sub_alarms WHERE isEnabled = 1")
    fun getEnabledSubAlarms(): Flow<List<SubAlarm>>
    
    /**
     * 删除指定主闹钟的所有子闹钟
     * @param parentAlarmId 主闹钟ID
     */
    @Query("DELETE FROM sub_alarms WHERE parentAlarmId = :parentAlarmId")
    suspend fun deleteAllSubAlarmsByParentId(parentAlarmId: Long)
    
    /**
     * 修复孤立的子闹钟
     * 删除没有对应主闹钟的子闹钟
     */
    @Query("""
        DELETE FROM sub_alarms 
        WHERE parentAlarmId NOT IN (SELECT id FROM alarms)
    """)
    suspend fun fixOrphanedSubAlarms()
} 