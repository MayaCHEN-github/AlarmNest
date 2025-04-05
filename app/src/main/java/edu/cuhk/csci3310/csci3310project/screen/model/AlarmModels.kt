package edu.cuhk.csci3310.csci3310project.screen.model

data class AlarmData(
    val time: String,
    val description: String,
    val initialEnabled: Boolean = true,
    val subAlarms: List<SubAlarmData> = emptyList()
)

data class SubAlarmData(
    val timeDiff: String,  // 例如 "+1:00"
    val description: String,
    val triggerType: String? = null, // "keyboard", "list", "walk" 或 null
    val enabled: Boolean = true
) 