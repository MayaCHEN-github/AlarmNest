package edu.cuhk.csci3310.csci3310project.alarm.storage

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.util.*
import java.io.File
import java.util.concurrent.locks.ReentrantLock
import edu.cuhk.csci3310.csci3310project.MainActivity

// 对数据库进行封装，提供统一的访问接口
object AlarmDatabaseFacade {
    
    private const val TAG = "AlarmDatabaseFacade"
    private var repository: AlarmRepository? = null
    private val lock = ReentrantLock()
    private var isInitializing = false
    
    // 初始化数据库。如果仓库实例不存在，则创建
    fun initialize(context: Context) {
        if (repository != null) {
            Log.d(TAG, "Repository already initialized仓库已初始化")
            return
        }
        
        if (isInitializing) {
            Log.w(TAG, "Repository is being initialized by another thread仓库正在被其他线程初始化")
            return
        }
        
        try {
            lock.lock()
            isInitializing = true
            
            if (repository != null) {
                Log.d(TAG, "Repository was initialized by another thread while waiting仓库已被其他线程初始化")
                return
            }
            
            // 检查应用权限
            if (context is MainActivity && !context.isStoragePermissionGranted()) {
                Log.e(TAG, "缺少存储权限")
                throw SecurityException("缺少存储权限")
            }
            
            // 检查数据库目录权限
            val dbPath = context.getDatabasePath("alarm_database")
            val parentFile = dbPath.parentFile
            if (parentFile != null && !parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    Log.e(TAG, "无法创建数据库目录")
                    throw SecurityException("无法创建数据库目录")
                }
            }
            
            Log.d(TAG, "Starting repository initialization开始初始化仓库")
            val database = AppDatabase.getDatabase(context)
            
            // 验证数据库是否成功创建
            if (database == null) {
                Log.e(TAG, "数据库创建失败")
                throw IllegalStateException("数据库创建失败")
            }
            
            repository = AlarmRepository(database.alarmDao(), database.subAlarmDao())
            
            // 验证repository是否成功创建
            if (repository == null) {
                Log.e(TAG, "Repository创建失败")
                throw IllegalStateException("Repository创建失败")
            }
            
            Log.d(TAG, "Repository initialization completed仓库初始化完成")
            
        } catch (e: SecurityException) {
            Log.e(TAG, "权限错误: ${e.message}", e)
            repository = null
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Repository initialization failed仓库初始化失败: ${e.message}", e)
            repository = null
            throw e
        } finally {
            isInitializing = false
            lock.unlock()
        }
    }
    
    // 获取仓库实例
    fun getRepository(context: Context): AlarmRepository? {
        if (repository == null) {
            Log.w(TAG, "Repository is null, attempting to initialize仓库为空，尝试初始化")
            try {
                initialize(context)
            } catch (e: Exception) {
                Log.e(TAG, "Repository initialization failed, context: ${context.javaClass.simpleName}仓库初始化失败，上下文：${context.javaClass.simpleName}", e)
                return null
            }
        }
        
        return repository ?: run {
            Log.e(TAG, "Repository initialization failed, context: ${context.javaClass.simpleName}仓库初始化失败，上下文：${context.javaClass.simpleName}")
            null
        }
    }
    
    // 获取所有闹钟
    fun getAllAlarms(context: Context): Flow<List<Alarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get all alarms获取所有闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.allAlarms
        } catch (e: Exception) {
            Log.e(TAG, "get all alarms获取所有闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 获取所有启用的闹钟
    fun getEnabledAlarms(context: Context): Flow<List<Alarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get enabled alarms获取启用闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.enabledAlarms
        } catch (e: Exception) {
            Log.e(TAG, "get enabled alarms获取启用闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 添加新闹钟
    suspend fun addAlarm(
        context: Context,
        calendar: Calendar,
        repeatType: RepeatType = RepeatType.ONCE,
        customDays: String? = null,
        dismissType: DismissType = DismissType.NO_ALARM,
        triggerType: TriggerType = TriggerType.TIME,
        label: String = "",
        customIntervalDays: Int = 0
    ): Long {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "add alarm添加闹钟失败：仓库不存在")
                return -1
            }
            
            val alarm = repo.createAlarmFromCalendar(
                calendar,
                repeatType,
                customDays,
                dismissType,
                triggerType,
                label,
                customIntervalDays
            )
            
            repo.insert(alarm)
        } catch (e: Exception) {
            Log.e(TAG, "add alarm添加闹钟失败: ${e.message}", e)
            -1
        }
    }
    
    // 更新闹钟
    suspend fun updateAlarm(context: Context, alarm: Alarm) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "update alarm更新闹钟失败：仓库不存在")
                return
            }
            
            repo.update(alarm)
        } catch (e: Exception) {
            Log.e(TAG, "update alarm更新闹钟失败: ${e.message}", e)
        }
    }
    
    // 删除闹钟
    suspend fun deleteAlarm(context: Context, alarm: Alarm) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "delete alarm删除闹钟失败：仓库不存在")
                return
            }
            
            repo.delete(alarm)
        } catch (e: Exception) {
            Log.e(TAG, "delete alarm删除闹钟失败: ${e.message}", e)
        }
    }
    
    // 根据ID获取闹钟
    suspend fun getAlarmById(context: Context, id: Long): Alarm? {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get alarm by id获取闹钟失败：仓库不存在")
                return null
            }
            
            repo.getAlarmById(id)
        } catch (e: Exception) {
            Log.e(TAG, "get alarm by id获取闹钟失败: ${e.message}", e)
            null
        }
    }
    
    // 切换闹钟的启用状态
    suspend fun toggleAlarmEnabled(context: Context, alarm: Alarm) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "toggle alarm切换闹钟状态失败：仓库不存在")
                return
            }
            
            val updatedAlarm = alarm.copy(isEnabled = !alarm.isEnabled)
            repo.update(updatedAlarm)
        } catch (e: Exception) {
            Log.e(TAG, "toggle alarm切换闹钟状态失败: ${e.message}", e)
        }
    }
    
    // 获取指定主闹钟的所有子闹钟
    fun getSubAlarms(context: Context, parentAlarmId: Long): Flow<List<SubAlarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get sub alarms获取子闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.getSubAlarmsByParentId(parentAlarmId)
        } catch (e: Exception) {
            Log.e(TAG, "get sub alarms获取子闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 添加新子闹钟
    suspend fun addSubAlarm(
        context: Context,
        parentAlarmId: Long,
        timeOffsetMinutes: Int,
        dismissType: DismissType = DismissType.NO_ALARM,
        label: String = "",
        absoluteTriggerTime: Long? = null
    ): Long {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "add sub alarm添加子闹钟失败：仓库不存在")
                return -1
            }
            
            val subAlarm = repo.createSubAlarm(
                parentAlarmId,
                timeOffsetMinutes,
                dismissType,
                label,
                absoluteTriggerTime
            )
            
            repo.insertSubAlarm(subAlarm)
        } catch (e: Exception) {
            Log.e(TAG, "add sub alarm添加子闹钟失败: ${e.message}", e)
            -1
        }
    }
    
    // 更新子闹钟
    suspend fun updateSubAlarm(context: Context, subAlarm: SubAlarm) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "update sub alarm更新子闹钟失败：仓库不存在")
                return
            }
            
            repo.updateSubAlarm(subAlarm)
        } catch (e: Exception) {
            Log.e(TAG, "update sub alarm更新子闹钟失败: ${e.message}", e)
        }
    }
    
    // 删除子闹钟
    suspend fun deleteSubAlarm(context: Context, subAlarm: SubAlarm) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "delete sub alarm删除子闹钟失败：仓库不存在")
                return
            }
            
            repo.deleteSubAlarm(subAlarm)
        } catch (e: Exception) {
            Log.e(TAG, "delete sub alarm删除子闹钟失败: ${e.message}", e)
        }
    }
    
    // 删除指定主闹钟的所有子闹钟
    suspend fun deleteAllSubAlarms(context: Context, parentAlarmId: Long) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "delete all sub alarms删除所有子闹钟失败：仓库不存在")
                return
            }
            
            repo.deleteAllSubAlarmsByParentId(parentAlarmId)
        } catch (e: Exception) {
            Log.e(TAG, "delete all sub alarms删除所有子闹钟失败: ${e.message}", e)
        }
    }
    
    // 根据重复类型获取闹钟
    fun getAlarmsByRepeatType(context: Context, repeatType: RepeatType): Flow<List<Alarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get alarms by repeat type根据重复类型获取闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.getAlarmsByRepeatType(repeatType)
        } catch (e: Exception) {
            Log.e(TAG, "get alarms by repeat type根据重复类型获取闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 根据触发类型获取闹钟
    fun getAlarmsByTriggerType(context: Context, triggerType: TriggerType): Flow<List<Alarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get alarms by trigger type根据触发类型获取闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.getAlarmsByTriggerType(triggerType)
        } catch (e: Exception) {
            Log.e(TAG, "get alarms by trigger type根据触发类型获取闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 根据关闭方式获取闹钟
    fun getAlarmsByDismissType(context: Context, dismissType: DismissType): Flow<List<Alarm>> {
        return try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "get alarms by dismiss type根据关闭方式获取闹钟失败：仓库不存在")
                return flowOf(emptyList())
            }
            
            repo.getAlarmsByDismissType(dismissType)
        } catch (e: Exception) {
            Log.e(TAG, "get alarms by dismiss type根据关闭方式获取闹钟失败: ${e.message}", e)
            flowOf(emptyList())
        }
    }
    
    // 执行数据库维护
    suspend fun performMaintenance(context: Context) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "perform maintenance执行数据库维护失败：仓库不存在")
                return
            }
            
            AppDatabase.performMaintenance(context)
        } catch (e: Exception) {
            Log.e(TAG, "perform maintenance执行数据库维护失败: ${e.message}", e)
        }
    }
    
    // 备份数据库
    suspend fun backupDatabase(context: Context, destination: File) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "backup database备份数据库失败：仓库不存在")
                return
            }
            
            AppDatabase.backupDatabase(context, destination)
        } catch (e: Exception) {
            Log.e(TAG, "backup database备份数据库失败: ${e.message}", e)
        }
    }
    
    // 初始化数据库
    suspend fun initializeDatabase(context: Context) {
        try {
            val repo = getRepository(context)
            if (repo == null) {
                Log.e(TAG, "initialize database初始化数据库失败：仓库不存在")
                return
            }
            
            // 1. 执行数据库维护
            performMaintenance(context)
            
            // 2. 创建备份目录
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) {
                if (!backupDir.mkdirs()) {
                    Log.e(TAG, "无法创建备份目录")
                    return
                }
            }
            
            // 3. 创建备份文件
            val backupFile = File(backupDir, "alarm_backup_${System.currentTimeMillis()}.db")
            backupDatabase(context, backupFile)
        } catch (e: Exception) {
            Log.e(TAG, "initialize database初始化数据库失败: ${e.message}", e)
        }
    }
} 