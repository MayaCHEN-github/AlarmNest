package edu.cuhk.csci3310.csci3310project.backend.data

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

/* AlarmDatabase.kt:
 * 这个文件定义了一个Room数据库类AppDatabase，用于存储闹钟和子闹钟数据。
 * */

// 数据库类，提供数据库访问接口。
@Database(
    entities = [Alarm::class, SubAlarm::class], // 数据库当中包含表Alarm与SubAlarm
    version = 5, // 数据库版本号（更新会销毁原来的全部数据，因为我懒得写迁移了……）
    exportSchema = true // 导出数据库架构，用于版本控制（如果日后会写的话……）
)
abstract class AppDatabase : RoomDatabase() {

    // 定义数据访问对象(DAO)接口
    abstract fun alarmDao(): AlarmDao // 主闹钟DAO
    abstract fun subAlarmDao(): SubAlarmDao // 子闹钟DAO
    
    companion object {
        private const val TAG = "AppDatabase"
        
        @Volatile // 确保多线程不会同时创建多个实例，因为数据库只能有一个！
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
                            db.query("PRAGMA journal_mode = WAL").use { cursor ->
                                cursor.moveToFirst()  // 日志模式为WAL，Write-Ahead Logging
                            }
                            
                            db.query("PRAGMA foreign_keys = ON").use { cursor ->
                                cursor.moveToFirst()  // 外键约束开启
                            }

                            Log.d(TAG, "闹钟数据库创建完成")
                            
                            CoroutineScope(Dispatchers.IO).launch {
                                // TODO:之后可以在这里添加一些初始数据！
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "数据库初始化失败: ${e.message}", e)
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
                                    Log.w(TAG, "发现无效的闹钟时间数据")
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
                                    Log.w(TAG, "发现孤立的子闹钟数据")
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
                                    Log.d(TAG, "发现需要处理的过期闹钟")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "数据库打开失败: ${e.message}", e)
                        }
                    }
                })
                .fallbackToDestructiveMigration() // 如果迁移失败，则重新创建数据库（会丢失数据！）
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
                
                Log.d(TAG, "数据库维护完成")
            }
        }
        
        // 数据库备份方法
        suspend fun backupDatabase(context: Context, destination: File) {
            val db = getDatabase(context)
            withContext(Dispatchers.IO) {
                // 执行数据库备份
                db.query("VACUUM INTO ?", arrayOf(destination.absolutePath))
                Log.d(TAG, "数据库备份完成: ${destination.absolutePath}")
            }
        }
    }
} 