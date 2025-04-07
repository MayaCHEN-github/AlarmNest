package edu.cuhk.csci3310.csci3310project.backend.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/* AlarmDao.kt:
 *  闹钟与子闹钟的数据访问对象(DAO)接口。
 * */


// 主闹钟的数据访问对象(DAO)接口
@Dao
interface AlarmDao {

    // 获取所有闹钟，按小时和分钟排序
    @Query("SELECT * FROM alarms ORDER BY hour, minute")
    fun getAllAlarms(): Flow<List<Alarm>> // flow是一种可观察的数据流，允许我们在数据变化时自动更新UI.
    
    // 插入新闹钟
    @Insert
    suspend fun insertAlarm(alarm: Alarm): Long
    
    // 更新闹钟
    @Update
    suspend fun updateAlarm(alarm: Alarm)
    
    // 删除闹钟
    @Delete
    suspend fun deleteAlarm(alarm: Alarm)
    
    // 根据ID获取闹钟
    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Long): Alarm?
    
    // 获取所有启用的闹钟
    @Query("SELECT * FROM alarms WHERE isEnabled = 1")
    fun getEnabledAlarms(): Flow<List<Alarm>>
    
    // 根据重复类型获取闹钟
    @Query("SELECT * FROM alarms WHERE repeatType = :repeatType")
    fun getAlarmsByRepeatType(repeatType: RepeatType): Flow<List<Alarm>>
    
    // 根据触发方式(time、location)获取闹钟
    @Query("SELECT * FROM alarms WHERE triggerType = :triggerType")
    fun getAlarmsByTriggerType(triggerType: TriggerType): Flow<List<Alarm>>
    
    // 根据关闭方式（no,typing,walking,checklist）获取闹钟
    @Query("SELECT * FROM alarms WHERE dismissType = :dismissType")
    fun getAlarmsByDismissType(dismissType: DismissType): Flow<List<Alarm>>
    
    // 清理过期的闹钟
    @Query("""
        DELETE FROM alarms 
        WHERE isEnabled = 1 
        AND lastTriggered IS NOT NULL 
        AND lastTriggered < :thresholdTime
    """)
    suspend fun cleanupExpiredAlarms(thresholdTime: Long = System.currentTimeMillis() - 24 * 60 * 60 * 1000)
}

// 子闹钟的数据访问对象(DAO)接口
@Dao
interface SubAlarmDao {

    // 获取所有子闹钟，按时间偏移量排序
    @Query("SELECT * FROM sub_alarms WHERE parentAlarmId = :parentAlarmId ORDER BY timeOffsetMinutes")
    fun getSubAlarmsByParentId(parentAlarmId: Long): Flow<List<SubAlarm>>
    
    // 插入新子闹钟
    @Insert
    suspend fun insertSubAlarm(subAlarm: SubAlarm): Long
    
    // 更新子闹钟
    @Update
    suspend fun updateSubAlarm(subAlarm: SubAlarm)
    
    // 删除子闹钟
    @Delete
    suspend fun deleteSubAlarm(subAlarm: SubAlarm)
    
    // 根据ID获取子闹钟
    @Query("SELECT * FROM sub_alarms WHERE id = :subAlarmId")
    suspend fun getSubAlarmById(subAlarmId: Long): SubAlarm?
    
    // 获取所有启用的子闹钟
    @Query("SELECT * FROM sub_alarms WHERE isEnabled = 1")
    fun getEnabledSubAlarms(): Flow<List<SubAlarm>>
    
    // 删除指定主闹钟的所有子闹钟
    @Query("DELETE FROM sub_alarms WHERE parentAlarmId = :parentAlarmId")
    suspend fun deleteAllSubAlarmsByParentId(parentAlarmId: Long)
    
    // 清理孤立的子闹钟（没有对应的主闹钟）
    @Query("""
        DELETE FROM sub_alarms 
        WHERE parentAlarmId NOT IN (SELECT id FROM alarms)
    """)
    suspend fun fixOrphanedSubAlarms()
} 