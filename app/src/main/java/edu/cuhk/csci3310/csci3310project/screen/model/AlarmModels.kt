package edu.cuhk.csci3310.csci3310project.screen.model

import edu.cuhk.csci3310.csci3310project.alarm.storage.RepeatType

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
) {
}

data class SubAlarmData(
    val timeDiff: String,  // 例如 "+1:00"
    val description: String,
    val triggerType: String? = null, // "keyboard", "list", "walk" 或 null
    val enabled: Boolean = true
) 