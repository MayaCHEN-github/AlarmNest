package edu.cuhk.csci3310.csci3310project.screen.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cuhk.csci3310.csci3310project.alarm.storage.*
import edu.cuhk.csci3310.csci3310project.screen.model.AlarmData
import edu.cuhk.csci3310.csci3310project.screen.model.SubAlarmData
import edu.cuhk.csci3310.csci3310project.alarm.AlarmManager
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
                android.util.Log.d("ClockListScreenViewModel", "开始加载闹钟数据")
                _uiState.update { it.copy(isLoading = true) }
                
                // 获取所有闹钟
                AlarmDatabaseFacade.getAllAlarms(context)
                    .collect { alarms ->
                        android.util.Log.d("ClockListScreenViewModel", "从数据库获取到闹钟: $alarms")
                        
                        val alarmDataList = alarms.map { alarm ->
                            convertToAlarmData(alarm)
                        }
                        
                        android.util.Log.d("ClockListScreenViewModel", "转换后的闹钟数据列表: $alarmDataList")
                        
                        _uiState.update { 
                            it.copy(
                                alarms = alarmDataList,
                                timeUntilNextAlarm = calculateNextAlarmTime(alarms),
                                isLoading = false
                            )
                        }
                        
                        android.util.Log.d("ClockListScreenViewModel", "UI状态已更新")
                    }
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "加载闹钟失败", e)
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
        android.util.Log.d("ClockListScreenViewModel", "转换Alarm到AlarmData: $alarm")
        
        // 获取子闹钟
        val subAlarms = AlarmDatabaseFacade.getSubAlarms(context, alarm.id)
            .first() // 获取第一个值
            .map { subAlarm ->
                convertToSubAlarmData(subAlarm, alarm)
            }
        
        val alarmData = AlarmData(
            id = alarm.id,
            time = String.format("%02d:%02d", alarm.hour, alarm.minute),
            description = alarm.label,
            initialEnabled = alarm.isEnabled,
            subAlarms = subAlarms,
            repeatType = alarm.repeatType,
            customDays = alarm.customDays,
            dayOfMonth = alarm.dayOfMonth,
            month = alarm.month
        )
        
        android.util.Log.d("ClockListScreenViewModel", "转换后的AlarmData: $alarmData")
        return alarmData
    }
    
    // 转换SubAlarm实体到SubAlarmData
    private fun convertToSubAlarmData(subAlarm: SubAlarm, parentAlarm: Alarm): SubAlarmData {
        // 计算时间差显示
        val timeDiff = if (subAlarm.timeOffsetMinutes >= 0) "+" else "-"
        val absMinutes = subAlarm.timeOffsetMinutes.absoluteValue
        val hours = absMinutes / 60
        val minutes = absMinutes % 60
        val timeDiffStr = String.format("%s%02d:%02d", timeDiff, hours, minutes)
        
        // 确定触发类型
        val triggerType = when (subAlarm.dismissType) {
            DismissType.TEXT_ALARM -> "keyboard"
            DismissType.CHECKLIST_ALARM -> "list"
            DismissType.WALK_ALARM -> "walk"
            else -> null
        }
        
        return SubAlarmData(
            id = subAlarm.id,
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
            }
            
            // 计算下一个触发时间
            val nextTriggerTime = AlarmManager.calculateNextTriggerTime(alarmTime, alarm)
            if (nextTriggerTime == null) {
                return@minByOrNull Long.MAX_VALUE
            }
            nextTriggerTime.timeInMillis - now.timeInMillis
        }
        
        if (nextAlarm == null) {
            return "No alarm enabled.\nEnjoy your time!"
        }
        
        // 计算下一个触发时间
        val nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, nextAlarm.hour)
            set(Calendar.MINUTE, nextAlarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val nextTriggerTime = AlarmManager.calculateNextTriggerTime(nextTime, nextAlarm)
        if (nextTriggerTime == null) {
            return "No alarm enabled.\nEnjoy your time!"
        }
        
        val diffMillis = nextTriggerTime.timeInMillis - now.timeInMillis
        val diffMinutes = diffMillis / (1000 * 60)
        val hours = diffMinutes / 60
        val minutes = diffMinutes % 60
        
        // 计算天数差
        val days = diffMinutes / (24 * 60)
        
        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""} $hours hour${if (hours > 1) "s" else ""}\ntill the next alarm..."
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} $minutes minute${if (minutes > 1) "s" else ""}\ntill the next alarm..."
            minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""}\ntill the next alarm..."
            else -> "Alarm is about to ring..."
        }
    }
    
    // 切换闹钟启用状态
    fun toggleAlarmEnabled(alarm: Alarm) {
        viewModelScope.launch {
            try {
                // 更新数据库中的闹钟状态
                AlarmDatabaseFacade.toggleAlarmEnabled(context, alarm)
                
                // 如果闹钟被启用，设置系统闹钟
                if (alarm.isEnabled) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, alarm.hour)
                        set(Calendar.MINUTE, alarm.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    AlarmManager.setAlarm(context, calendar, alarm)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "更新闹钟状态失败: ${e.message}")
                }
            }
        }
    }
    
    // 更新闹钟重复模式
    fun updateAlarmRepeatType(alarm: Alarm, repeatType: RepeatType, customDays: String?) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新闹钟重复模式: alarmId=${alarm.id}, repeatType=$repeatType, customDays=$customDays")
                
                val updatedAlarm = when(repeatType) {
                    RepeatType.MONTHLY -> alarm.copy(
                        repeatType = repeatType,
                        customDays = null,
                        dayOfMonth = customDays?.toIntOrNull(),
                        month = null
                    )
                    RepeatType.YEARLY -> {
                        val (month, day) = customDays?.split(",")?.map { it.toIntOrNull() } ?: listOf(null, null)
                        alarm.copy(
                            repeatType = repeatType,
                            customDays = null,
                            dayOfMonth = day,
                            month = month
                        )
                    }
                    else -> alarm.copy(
                        repeatType = repeatType,
                        customDays = customDays,
                        dayOfMonth = null,
                        month = null
                    )
                }
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的闹钟数据: $updatedAlarm")
                AlarmDatabaseFacade.updateAlarm(context, updatedAlarm)
                
                // 如果闹钟是启用的，设置系统闹钟
                if (updatedAlarm.isEnabled) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, updatedAlarm.hour)
                        set(Calendar.MINUTE, updatedAlarm.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    AlarmManager.setAlarm(context, calendar, updatedAlarm)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "数据库更新成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新闹钟重复模式失败", e)
                _uiState.update { 
                    it.copy(error = "更新闹钟重复模式失败: ${e.message}")
                }
            }
        }
    }
    
    // 更新闹钟时间
    fun updateAlarmTime(alarm: Alarm, hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新闹钟时间: alarmId=${alarm.id}, hour=$hour, minute=$minute")
                
                val updatedAlarm = alarm.copy(
                    hour = hour,
                    minute = minute
                )
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的闹钟数据: $updatedAlarm")
                AlarmDatabaseFacade.updateAlarm(context, updatedAlarm)
                
                // 如果闹钟是启用的，设置系统闹钟
                if (updatedAlarm.isEnabled) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    AlarmManager.setAlarm(context, calendar, updatedAlarm)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "数据库更新成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新闹钟时间失败", e)
                _uiState.update { 
                    it.copy(error = "更新闹钟时间失败: ${e.message}")
                }
            }
        }
    }
    
    // 更新闹钟描述
    fun updateAlarmDescription(alarm: Alarm, description: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新闹钟描述: alarmId=${alarm.id}, description=$description")
                
                val updatedAlarm = alarm.copy(
                    label = description
                )
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的闹钟数据: $updatedAlarm")
                AlarmDatabaseFacade.updateAlarm(context, updatedAlarm)
                
                // 如果闹钟是启用的，重新设置系统闹钟以确保状态正确
                if (updatedAlarm.isEnabled) {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, updatedAlarm.hour)
                        set(Calendar.MINUTE, updatedAlarm.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    AlarmManager.setAlarm(context, calendar, updatedAlarm)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "数据库更新成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新闹钟描述失败", e)
                _uiState.update { 
                    it.copy(error = "更新闹钟描述失败: ${e.message}")
                }
            }
        }
    }
    
    // 创建测试数据
    fun createTestData() {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始创建测试数据")
                
                // 创建每日闹钟
                val dailyCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "创建每日闹钟: time=08:00")
                
                val dailyAlarmId = AlarmDatabaseFacade.addAlarm(
                    context = context,
                    calendar = dailyCalendar,
                    repeatType = RepeatType.DAILY,
                    label = "Morning Alarm",
                    dismissType = DismissType.TEXT_ALARM
                )
                
                android.util.Log.d("ClockListScreenViewModel", "每日闹钟创建成功: id=$dailyAlarmId")
                
                if (dailyAlarmId > 0) {
                    // 添加子闹钟
                    android.util.Log.d("ClockListScreenViewModel", "开始添加子闹钟")
                    
                    // 提前30分钟的清单闹钟
                    val subAlarm1Id = AlarmDatabaseFacade.addSubAlarm(
                        context = context,
                        parentAlarmId = dailyAlarmId,
                        timeOffsetMinutes = -30,
                        dismissType = DismissType.CHECKLIST_ALARM,
                        label = "Prepare for the day"
                    )
                    
                    android.util.Log.d("ClockListScreenViewModel", "子闹钟1创建成功: id=$subAlarm1Id, timeOffset=-30")
                    
                    // 提前15分钟的走路闹钟
                    val subAlarm2Id = AlarmDatabaseFacade.addSubAlarm(
                        context = context,
                        parentAlarmId = dailyAlarmId,
                        timeOffsetMinutes = -15,
                        dismissType = DismissType.WALK_ALARM,
                        label = "Wake up and stretch"
                    )
                    
                    android.util.Log.d("ClockListScreenViewModel", "子闹钟2创建成功: id=$subAlarm2Id, timeOffset=-15")
                }
                
                // 创建工作日闹钟
                val weekdayCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "创建工作日闹钟: time=09:00")
                
                AlarmDatabaseFacade.addAlarm(
                    context = context,
                    calendar = weekdayCalendar,
                    repeatType = RepeatType.WEEKDAYS,
                    label = "Work Time",
                    dismissType = DismissType.TEXT_ALARM
                )
                
                // 创建周末闹钟
                val weekendCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "创建周末闹钟: time=10:00")
                
                AlarmDatabaseFacade.addAlarm(
                    context = context,
                    calendar = weekendCalendar,
                    repeatType = RepeatType.WEEKENDS,
                    label = "Weekend Alarm",
                    dismissType = DismissType.TEXT_ALARM
                )
                
                // 创建自定义闹钟(周一、周三、周五)
                val customCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 15)
                    set(Calendar.MINUTE, 0)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "创建自定义闹钟: time=15:00, days=1,3,5")
                
                AlarmDatabaseFacade.addAlarm(
                    context = context,
                    calendar = customCalendar,
                    repeatType = RepeatType.CUSTOM,
                    customDays = "1,3,5",
                    label = "Custom Alarm",
                    dismissType = DismissType.TEXT_ALARM
                )
                
                // 创建单次闹钟
                val onceCalendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 22)
                    set(Calendar.MINUTE, 0)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "创建单次闹钟: time=22:00")
                
                AlarmDatabaseFacade.addAlarm(
                    context = context,
                    calendar = onceCalendar,
                    repeatType = RepeatType.ONCE,
                    label = "One-time Alarm",
                    dismissType = DismissType.TEXT_ALARM
                )
                
                android.util.Log.d("ClockListScreenViewModel", "所有测试数据创建完成")
                
                // 重新加载闹钟列表
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "创建测试数据失败", e)
                _uiState.update { 
                    it.copy(error = "创建测试数据失败: ${e.message}")
                }
            }
        }
    }
    
    // 删除闹钟
    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始删除闹钟: alarmId=${alarm.id}")
                
                // 从数据库删除闹钟
                AlarmDatabaseFacade.deleteAlarm(context, alarm)
                
                android.util.Log.d("ClockListScreenViewModel", "闹钟删除成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "删除闹钟失败", e)
                _uiState.update { 
                    it.copy(error = "删除闹钟失败: ${e.message}")
                }
            }
        }
    }
    
    // 删除子闹钟
    fun deleteSubAlarm(parentAlarm: Alarm, subAlarm: SubAlarm) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始删除子闹钟: subAlarmId=${subAlarm.id}")
                
                // 从数据库删除子闹钟
                AlarmDatabaseFacade.deleteSubAlarm(context, subAlarm)
                
                android.util.Log.d("ClockListScreenViewModel", "子闹钟删除成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "删除子闹钟失败", e)
                _uiState.update { 
                    it.copy(error = "删除子闹钟失败: ${e.message}")
                }
            }
        }
    }
    
    // 获取子闹钟ID
    fun getSubAlarmId(
        parentAlarmId: Long,
        timeDiff: String,
        description: String,
        triggerType: String?,
        enabled: Boolean,
        callback: (Long?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始获取子闹钟ID: parentAlarmId=$parentAlarmId, timeDiff=$timeDiff")
                
                // 获取所有子闹钟
                val subAlarms = AlarmDatabaseFacade.getSubAlarms(context, parentAlarmId)
                    .first() // 获取第一个值
                
                android.util.Log.d("ClockListScreenViewModel", "获取到的子闹钟列表: $subAlarms")
                
                // 计算时间偏移
                val timeOffsetMinutes = timeDiff.let { diff ->
                    val sign = if (diff.startsWith("+")) 1 else -1
                    val parts = diff.substring(1).split(":")
                    val hours = parts[0].toInt()
                    val minutes = if (parts.size > 1) parts[1].toInt() else 0
                    sign * (hours * 60 + minutes)
                }
                
                android.util.Log.d("ClockListScreenViewModel", "计算得到的时间偏移: $timeOffsetMinutes")
                
                // 确定关闭方式
                val dismissType = when (triggerType) {
                    "keyboard" -> DismissType.TEXT_ALARM
                    "list" -> DismissType.CHECKLIST_ALARM
                    "walk" -> DismissType.WALK_ALARM
                    else -> DismissType.NO_ALARM
                }
                
                android.util.Log.d("ClockListScreenViewModel", "匹配条件: timeOffsetMinutes=$timeOffsetMinutes")
                
                // 查找匹配的子闹钟 - 主要匹配时间偏移
                val subAlarm = subAlarms.find { subAlarm ->
                    subAlarm.timeOffsetMinutes == timeOffsetMinutes
                }
                
                if (subAlarm == null) {
                    android.util.Log.e("ClockListScreenViewModel", "未找到匹配的子闹钟")
                } else {
                    android.util.Log.d("ClockListScreenViewModel", "找到匹配的子闹钟: $subAlarm")
                }
                
                callback(subAlarm?.id)
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "获取子闹钟ID失败", e)
                callback(null)
            }
        }
    }
    
    // 更新子闹钟时间差
    fun updateSubAlarmTimeDiff(parentAlarm: Alarm, subAlarm: SubAlarm, newTimeDiffMinutes: Int) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新子闹钟时间差: subAlarmId=${subAlarm.id}, newTimeDiffMinutes=$newTimeDiffMinutes")
                android.util.Log.d("ClockListScreenViewModel", "当前子闹钟数据: $subAlarm")
                
                val updatedSubAlarm = subAlarm.copy(
                    timeOffsetMinutes = newTimeDiffMinutes
                )
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的子闹钟数据: $updatedSubAlarm")
                
                // 更新数据库
                AlarmDatabaseFacade.updateSubAlarm(context, updatedSubAlarm)
                
                // 验证更新是否成功
                val updatedAlarm = AlarmDatabaseFacade.getSubAlarms(context, parentAlarm.id)
                    .first()
                    .find { it.id == subAlarm.id }
                
                if (updatedAlarm == null || updatedAlarm.timeOffsetMinutes != newTimeDiffMinutes) {
                    throw IllegalStateException("数据库更新验证失败")
                }
                
                android.util.Log.d("ClockListScreenViewModel", "数据库更新成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新子闹钟时间差失败", e)
                _uiState.update { 
                    it.copy(error = "更新子闹钟时间差失败: ${e.message}")
                }
            }
        }
    }
    
    // 添加子闹钟
    fun addSubAlarm(parentAlarm: Alarm, subAlarm: SubAlarm) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始添加子闹钟: parentAlarmId=${parentAlarm.id}")
                
                // 添加子闹钟到数据库
                AlarmDatabaseFacade.addSubAlarm(
                    context = context,
                    parentAlarmId = parentAlarm.id,
                    timeOffsetMinutes = subAlarm.timeOffsetMinutes,
                    dismissType = subAlarm.dismissType,
                    label = subAlarm.label
                )
                
                android.util.Log.d("ClockListScreenViewModel", "子闹钟添加成功")
                
                // 重新加载闹钟列表以确保UI更新
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "添加子闹钟失败", e)
                _uiState.update { 
                    it.copy(error = "添加子闹钟失败: ${e.message}")
                }
            }
        }
    }
    
    // 更新子闹钟备注
    fun updateSubAlarmDescription(parentAlarm: Alarm, subAlarm: SubAlarm, newDescription: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新子闹钟备注")
                android.util.Log.d("ClockListScreenViewModel", "父闹钟ID: ${parentAlarm.id}")
                android.util.Log.d("ClockListScreenViewModel", "子闹钟ID: ${subAlarm.id}")
                android.util.Log.d("ClockListScreenViewModel", "新的备注: $newDescription")
                
                // 创建更新后的子闹钟对象
                val updatedSubAlarm = subAlarm.copy(
                    label = newDescription  // 使用新的描述值
                )
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的子闹钟数据: $updatedSubAlarm")
                
                // 更新子闹钟备注
                AlarmDatabaseFacade.updateSubAlarm(
                    context = context,
                    subAlarm = updatedSubAlarm
                )
                
                android.util.Log.d("ClockListScreenViewModel", "子闹钟备注更新成功")
                
                // 刷新UI
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新子闹钟备注失败", e)
                _uiState.update { 
                    it.copy(error = "更新子闹钟备注失败: ${e.message}")
                }
            }
        }
    }
    
    // 更新子闹钟触发方式
    fun updateSubAlarmTriggerType(parentAlarm: Alarm, subAlarm: SubAlarm, newTriggerType: String?) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ClockListScreenViewModel", "开始更新子闹钟触发方式")
                android.util.Log.d("ClockListScreenViewModel", "父闹钟ID: ${parentAlarm.id}")
                android.util.Log.d("ClockListScreenViewModel", "子闹钟ID: ${subAlarm.id}")
                android.util.Log.d("ClockListScreenViewModel", "新的触发方式: $newTriggerType")
                
                // 获取最新的子闹钟数据
                val latestSubAlarm = AlarmDatabaseFacade.getSubAlarms(context, parentAlarm.id)
                    .first()
                    .find { it.id == subAlarm.id }
                
                if (latestSubAlarm == null) {
                    android.util.Log.e("ClockListScreenViewModel", "未找到子闹钟")
                    return@launch
                }
                
                // 将触发方式转换为DismissType
                val newDismissType = when (newTriggerType) {
                    "keyboard" -> DismissType.TEXT_ALARM
                    "list" -> DismissType.CHECKLIST_ALARM
                    "walk" -> DismissType.WALK_ALARM
                    else -> DismissType.NO_ALARM
                }
                
                // 创建更新后的子闹钟对象,使用最新的数据
                val updatedSubAlarm = latestSubAlarm.copy(
                    dismissType = newDismissType
                )
                
                android.util.Log.d("ClockListScreenViewModel", "更新后的子闹钟数据: $updatedSubAlarm")
                
                // 更新子闹钟触发方式
                AlarmDatabaseFacade.updateSubAlarm(
                    context = context,
                    subAlarm = updatedSubAlarm
                )
                
                android.util.Log.d("ClockListScreenViewModel", "子闹钟触发方式更新成功")
                
                // 刷新UI
                loadAlarms()
            } catch (e: Exception) {
                android.util.Log.e("ClockListScreenViewModel", "更新子闹钟触发方式失败", e)
                _uiState.update { 
                    it.copy(error = "更新子闹钟触发方式失败: ${e.message}")
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

