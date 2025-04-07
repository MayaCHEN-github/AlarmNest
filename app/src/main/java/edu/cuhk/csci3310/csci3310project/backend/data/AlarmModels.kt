package edu.cuhk.csci3310.csci3310project.backend.data

/* AlarmData.kt
 * 这个文件定义了AlarmData和SubAlarmData数据类。
 * 用于存储和传递闹钟和子闹钟的数据。
 * 这个更多是用于clockListScreen的数据类。
 * */


data class AlarmData(
    val id: Long = 0,
    val time: String,
    val description: String,
    val initialEnabled: Boolean = true,
    val subAlarms: List<SubAlarmData> = emptyList(),
    val repeatType: RepeatType = RepeatType.ONCE,
    val customDays: String? = null,
    val dayOfMonth: Int? = null,
    val month: Int? = null
)


data class SubAlarmData(
    val id: Long = 0,
    val timeDiff: String,  // 例如 "+1:00"
    val description: String,
    val triggerType: String? = null, // "keyboard", "list", "walk" 或 null
    val enabled: Boolean = true
) 