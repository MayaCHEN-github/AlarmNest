package edu.cuhk.csci3310.csci3310project.screen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cuhk.csci3310.csci3310project.alarm.storage.*
import edu.cuhk.csci3310.csci3310project.screen.model.AlarmData
import edu.cuhk.csci3310.csci3310project.screen.model.SubAlarmData
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.absoluteValue

// UI状态类
data class ClockListUiState(
    val timeUntilNextAlarm: String = "",
    val alarms: List<AlarmData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

open class ClockListScreenViewModel(private val context: Context) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(ClockListUiState())
    open val uiState: StateFlow<ClockListUiState> = _uiState.asStateFlow()
    
    init {
        loadAlarms()
    }
    
    // 加载闹钟数据
    private fun loadAlarms() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                // 获取所有闹钟
                AlarmDatabaseFacade.getAllAlarms(context)
                    .collect { alarms ->
                        val alarmDataList = alarms.map { alarm ->
                            convertToAlarmData(alarm)
                        }
                        
                        _uiState.update { 
                            it.copy(
                                alarms = alarmDataList,
                                timeUntilNextAlarm = calculateNextAlarmTime(alarms),
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        error = "加载闹钟失败: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }
    
    // 转换Alarm实体到AlarmData
    private suspend fun convertToAlarmData(alarm: Alarm): AlarmData {
        // 获取子闹钟
        val subAlarms = AlarmDatabaseFacade.getSubAlarms(context, alarm.id)
            .first() // 获取第一个值
            .map { subAlarm ->
                convertToSubAlarmData(subAlarm, alarm)
            }
        
        return AlarmData(
            time = String.format("%02d:%02d", alarm.hour, alarm.minute),
            description = alarm.label,
            initialEnabled = alarm.isEnabled,
            subAlarms = subAlarms
        )
    }
    
    // 转换SubAlarm实体到SubAlarmData
    private fun convertToSubAlarmData(subAlarm: SubAlarm, parentAlarm: Alarm): SubAlarmData {
        // 计算时间差显示
        val timeDiff = if (subAlarm.timeOffsetMinutes >= 0) "+" else "-"
        val absMinutes = subAlarm.timeOffsetMinutes.absoluteValue
        val hours = absMinutes / 60
        val minutes = absMinutes % 60
        val timeDiffStr = if (hours > 0) {
            String.format("%s%d:%02d", timeDiff, hours, minutes)
        } else {
            String.format("%s%d", timeDiff, minutes)
        }
        
        // 确定触发类型
        val triggerType = when (subAlarm.dismissType) {
            DismissType.TEXT_ALARM -> "keyboard"
            DismissType.CHECKLIST_ALARM -> "list"
            DismissType.WALK_ALARM -> "walk"
            else -> null
        }
        
        return SubAlarmData(
            timeDiff = timeDiffStr,
            description = subAlarm.label,
            triggerType = triggerType,
            enabled = subAlarm.isEnabled
        )
    }
    
    // 计算下一个闹钟时间
    private fun calculateNextAlarmTime(alarms: List<Alarm>): String {
        val now = Calendar.getInstance()
        val enabledAlarms = alarms.filter { it.isEnabled }
        
        if (enabledAlarms.isEmpty()) {
            return "No alarm enabled.\nEnjoy your time!"
        }
        
        // 找到最近的闹钟
        val nextAlarm = enabledAlarms.minByOrNull { alarm ->
            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // 如果闹钟时间已经过去，设置为明天
                if (before(now)) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            alarmTime.timeInMillis - now.timeInMillis
        }
        
        if (nextAlarm == null) {
            return "No alarm enabled.\nEnjoy your time!"
        }
        
        // 计算时间差
        val nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, nextAlarm.hour)
            set(Calendar.MINUTE, nextAlarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val diffMillis = nextTime.timeInMillis - now.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        return when {
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} $minutes minute${if (minutes > 1) "s" else ""}\ntill the next alarm..."
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}\ntill the next alarm..."
            else -> "Alarm is about to ring..."
        }
    }
    
    // 切换闹钟启用状态
    fun toggleAlarmEnabled(alarm: Alarm) {
        viewModelScope.launch {
            try {
                AlarmDatabaseFacade.toggleAlarmEnabled(context, alarm)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "更新闹钟状态失败: ${e.message}")
                }
            }
        }
    }
    
    // ViewModel工厂
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClockListScreenViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClockListScreenViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

