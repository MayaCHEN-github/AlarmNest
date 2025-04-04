package edu.cuhk.csci3310.csci3310project.alarm.storage

import androidx.room.*

// 闹钟重复类型枚举
enum class RepeatType {
    ONCE,           // 仅一次
    DAILY,          // 每天
    WEEKDAYS,       // 工作日（周一至周五）
    WEEKENDS,       // 周末（周六和周日）
    WEEKLY,         // 每周特定日期
    MONTHLY,        // 每月特定日期
    YEARLY,         // 每年特定日期
    CUSTOM          // 自定义间隔
}

// 关闭闹钟方式枚举
enum class DismissType {
    NO_ALARM,       // 无需操作，自动关闭
    TEXT_ALARM,     // 需要输入文本
    WALK_ALARM,     // 需要走路
    CHECKLIST_ALARM // 需要完成清单
}

// 触发闹钟方式枚举
enum class TriggerType {
    TIME,           // 时间触发
    LOCATION        // 位置触发
}

// 主闹钟实体类
@Entity(
    tableName = "alarms",
    indices = [
        Index("hour", "minute"),  // 时间索引，用于按时间排序和查询
        Index("isEnabled"),        // 启用状态索引，用于查询启用/禁用的闹钟
        Index("repeatType"),       // 重复类型索引
        Index("dayOfMonth"),       // 每月日期索引
        Index("month"),           // 月份索引
        Index("customDays")       // 自定义日期索引
    ]
)
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 基本时间信息
    val hour: Int,
    val minute: Int,
    
    // 日期信息
    val dayOfMonth: Int? = null,  // 每月几号
    val month: Int? = null,       // 每年几月
    val customDays: String? = null, // 自定义重复日期，格式如"1,3,5"表示周一、周三、周五
    
    // 重复方式
    val repeatType: RepeatType = RepeatType.ONCE,
    
    // 关闭方式
    val dismissType: DismissType = DismissType.NO_ALARM,
    
    // 触发方式
    val triggerType: TriggerType = TriggerType.TIME,
    
    // 位置信息（当triggerType为LOCATION时使用）
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val triggerRadius: Float? = null, // 触发半径（米）
    
    // 其他信息
    val label: String = "", // 备注
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    
    // 自定义间隔（当repeatType为CUSTOM时使用）
    val customIntervalDays: Int = 0,
    
    // 上次触发时间
    val lastTriggered: Long? = null
)

// 子闹钟实体类
@Entity(
    tableName = "sub_alarms",
    foreignKeys = [
        ForeignKey(
            entity = Alarm::class,
            parentColumns = ["id"],
            childColumns = ["parentAlarmId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("parentAlarmId"),  // 关联索引，用于快速查找主闹钟的子闹钟
        Index("timeOffsetMinutes")  // 时间偏移索引，用于按时间排序
    ]
)
data class SubAlarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // 关联的主闹钟ID
    val parentAlarmId: Long,
    
    // 与主闹钟的时间差（分钟）
    val timeOffsetMinutes: Int,
    
    // 绝对触发时间（用于单次子闹钟）
    val absoluteTriggerTime: Long? = null,
    
    // 关闭方式
    val dismissType: DismissType = DismissType.NO_ALARM,
    
    // 备注
    val label: String = "",
    
    // 是否启用
    val isEnabled: Boolean = true,
    
    // 上次触发时间
    val lastTriggered: Long? = null
) 