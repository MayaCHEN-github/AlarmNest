package edu.cuhk.csci3310.csci3310project.alarm.storage

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SimpleSQLiteQuery
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

// 数据库类，提供数据库访问接口。
@Database(
    entities = [Alarm::class, SubAlarm::class], // 定义数据库当中包含的表
    version = 5, // 数据库版本号
    exportSchema = true // 导出数据库架构，用于版本控制
)
abstract class AppDatabase : RoomDatabase() {
    // 定义数据访问对象(DAO)接口
    abstract fun alarmDao(): AlarmDao // 主闹钟表
    abstract fun subAlarmDao(): SubAlarmDao // 子闹钟表
    
    companion object {
        @Volatile // 确保多线程不会同时创建多个实例
        private var INSTANCE: AppDatabase? = null // 数据库实例（唯一的）
        
        // 只能通过getDatabase()方法获取数据库实例！
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {  // Instance为null时，创建数据库实例。
                val instance = Room.databaseBuilder( // 创建数据库实例
                    context.applicationContext,
                    AppDatabase::class.java, 
                    "alarm_database" // 数据库名称
                )
                // 添加数据库创建和打开时的回调
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        
                        try {
                            // 在事务外设置PRAGMA
                            db.query("PRAGMA journal_mode = WAL").use { cursor ->
                                cursor.moveToFirst()
                            }
                            
                            db.query("PRAGMA foreign_keys = ON").use { cursor ->
                                cursor.moveToFirst()
                            }
                            
                            Log.d("Database", "闹钟数据库创建完成")
                            
                            CoroutineScope(Dispatchers.IO).launch {
                                // 可以在这里添加一些初始数据
                            }
                        } catch (e: Exception) {
                            Log.e("Database", "数据库初始化失败: ${e.message}", e)
                        }
                    }
                    
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        
                        try {
                            // 检查时间数据是否有效
                            db.query("""
                                SELECT COUNT(*) 
                                FROM alarms 
                                WHERE isEnabled = 1 
                                AND (hour < 0 OR hour > 23 OR minute < 0 OR minute > 59)
                            """).use { cursor ->
                                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                                    Log.w("Database", "发现无效的闹钟时间数据")
                                }
                            }
                            
                            // 检查子闹钟的关联是否完整
                            db.query("""
                                SELECT COUNT(*) 
                                FROM sub_alarms s 
                                LEFT JOIN alarms a ON s.parentAlarmId = a.id 
                                WHERE a.id IS NULL
                            """).use { cursor ->
                                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                                    Log.w("Database", "发现孤立的子闹钟数据")
                                }
                            }
                            
                            // 优化数据库性能
                            db.query("PRAGMA optimize").use { cursor ->
                                cursor.moveToFirst()
                            }
                            
                            // 检查并清理过期的闹钟
                            val currentTime = System.currentTimeMillis()
                            db.query("""
                                SELECT COUNT(*) 
                                FROM alarms 
                                WHERE isEnabled = 1 
                                AND lastTriggered IS NOT NULL 
                                AND lastTriggered < ?
                            """, arrayOf(currentTime - 24 * 60 * 60 * 1000)).use { cursor ->
                                if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                                    Log.d("Database", "发现需要处理的过期闹钟")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("Database", "数据库打开失败: ${e.message}", e)
                        }
                    }
                })
                .fallbackToDestructiveMigration() // 如果迁移失败，则重新创建数据库（会丢失数据）
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        // 数据库维护方法
        suspend fun performMaintenance(context: Context) {
            val db = getDatabase(context)
            withContext(Dispatchers.IO) {
                // 1. 清理过期的闹钟
                db.alarmDao().cleanupExpiredAlarms()
                
                // 2. 修复数据不一致
                db.subAlarmDao().fixOrphanedSubAlarms()
                
                // 3. 更新统计信息
                db.query(SimpleSQLiteQuery("ANALYZE"))
                
                Log.d("Database", "数据库维护完成")
            }
        }
        
        // 数据库备份方法
        suspend fun backupDatabase(context: Context, destination: File) {
            val db = getDatabase(context)
            withContext(Dispatchers.IO) {
                // 执行数据库备份
                db.query("VACUUM INTO ?", arrayOf(destination.absolutePath))
                Log.d("Database", "数据库备份完成: ${destination.absolutePath}")
            }
        }
    }
} 