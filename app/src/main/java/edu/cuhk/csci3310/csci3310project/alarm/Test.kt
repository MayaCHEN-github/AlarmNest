package edu.cuhk.csci3310.csci3310project.alarm

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import edu.cuhk.csci3310.csci3310project.MainActivity
import edu.cuhk.csci3310.csci3310project.alarm.storage.AlarmDatabaseFacade
import edu.cuhk.csci3310.csci3310project.alarm.storage.RepeatType
import edu.cuhk.csci3310.csci3310project.alarm.storage.DismissType
import edu.cuhk.csci3310.csci3310project.alarm.storage.TriggerType
import edu.cuhk.csci3310.csci3310project.alarm.storage.Alarm
import kotlinx.coroutines.launch
import java.util.*
import android.util.Log

@Composable
fun AlarmTest(activity: MainActivity, innerPadding: PaddingValues) {
    val scope = rememberCoroutineScope()
    var currentTime by remember { mutableStateOf<Calendar>(Calendar.getInstance()) }
    val alarms by AlarmDatabaseFacade.getAllAlarms(activity).collectAsState(initial = emptyList())
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var showTimePicker by remember { mutableStateOf<Boolean>(false) }
    
    // 初始化数据库
    LaunchedEffect(Unit) {
        AlarmDatabaseFacade.initializeDatabase(activity)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        // 添加闹钟按钮
        Button(
            onClick = {
                if (AlarmPermission.checkAndRequestPermissions(activity)) {
                    showTimePicker = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "添加闹钟")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 显示当前时间
        Text(
            text = "当前时间: ${String.format(Locale.getDefault(), "%02d:%02d", 
                currentTime.get(Calendar.HOUR_OF_DAY), 
                currentTime.get(Calendar.MINUTE))}"
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 显示闹钟列表
        Text(text = "闹钟列表:", style = MaterialTheme.typography.titleMedium)
        
        alarms.forEach { alarm ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // 显示闹钟时间
                    Text(
                        text = when (alarm.repeatType) {
                            RepeatType.DAILY -> "每天 ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.WEEKDAYS -> "工作日 ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.WEEKENDS -> "周末 ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.WEEKLY -> "每周${formatWeeklyDay(alarm.customDays)} ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.MONTHLY -> "每月${alarm.dayOfMonth}号 ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.YEARLY -> "每年${alarm.month}月${alarm.dayOfMonth}日 ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            RepeatType.CUSTOM -> "自定义${formatCustomDays(alarm.customDays)} ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                            else -> "时间: ${String.format(Locale.getDefault(), "%02d:%02d", alarm.hour, alarm.minute)}"
                        }
                    )
                    
                    // 显示闹钟标签
                    Text(text = "标签: ${alarm.label}")
                    
                    // 显示重复类型
                    Text(text = "重复: ${alarm.repeatType}")
                    
                    // 显示关闭方式
                    Text(text = "关闭方式: ${alarm.dismissType}")
                    
                    // 显示启用状态
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "启用:")
                        Switch(
                            checked = alarm.isEnabled,
                            onCheckedChange = { isChecked ->
                                scope.launch {
                                    AlarmDatabaseFacade.toggleAlarmEnabled(activity, alarm)
                                }
                            }
                        )
                    }
                    
                    // 编辑和删除按钮
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { editingAlarm = alarm },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(text = "编辑")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    AlarmDatabaseFacade.deleteAlarm(activity, alarm)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(text = "删除")
                        }
                    }
                }
            }
        }
    }
    
    // 时间选择对话框
    if (showTimePicker) {
        showTimePickerDialog(activity, currentTime) { selectedTime ->
            showTimePicker = false
            scope.launch {
                try {
                    // 如果是编辑模式，更新现有闹钟
                    if (editingAlarm != null) {
                        val updatedAlarm = editingAlarm!!.copy(
                            hour = selectedTime.get(Calendar.HOUR_OF_DAY),
                            minute = selectedTime.get(Calendar.MINUTE)
                        )
                        AlarmDatabaseFacade.updateAlarm(activity, updatedAlarm)
                        // 设置新的闹钟时间
                        AlarmManager.setAlarm(activity, selectedTime, updatedAlarm)
                    } else {
                        // 创建新闹钟
                        val alarmId = AlarmDatabaseFacade.addAlarm(
                            context = activity,
                            calendar = selectedTime,
                            label = "新闹钟",
                            repeatType = RepeatType.ONCE,
                            dismissType = DismissType.NO_ALARM,
                            triggerType = TriggerType.TIME
                        )
                        if (alarmId != -1L) {
                            // 获取新创建的闹钟并设置提醒
                            val newAlarm = AlarmDatabaseFacade.getAlarmById(activity, alarmId)
                            if (newAlarm != null) {
                                AlarmManager.setAlarm(activity, selectedTime, newAlarm)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AlarmTest", "设置闹钟失败: ${e.message}", e)
                }
            }
        }
    }
    
    // 编辑对话框
    editingAlarm?.let { alarm ->
        Dialog(onDismissRequest = { editingAlarm = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                var currentAlarmTime by remember { 
                    mutableStateOf(Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, alarm.hour)
                        set(Calendar.MINUTE, alarm.minute)
                        set(Calendar.SECOND, 0)
                    })
                }
                var showCustomDaysDialog by remember { mutableStateOf(false) }
                var selectedCustomDays by remember { mutableStateOf(alarm.customDays?.split(",")?.map { it.toInt() }?.toSet() ?: emptySet()) }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "编辑闹钟",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 时间选择
                    Button(
                        onClick = { 
                            showTimePickerDialog(activity, currentAlarmTime) { selectedTime ->
                                currentAlarmTime = selectedTime
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "选择时间: ${String.format(Locale.getDefault(), "%02d:%02d", 
                            currentAlarmTime.get(Calendar.HOUR_OF_DAY), 
                            currentAlarmTime.get(Calendar.MINUTE))}")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 标签输入
                    var label by remember { mutableStateOf<String>(alarm.label) }
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("标签") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 重复类型选择
                    var repeatType by remember { mutableStateOf<RepeatType>(alarm.repeatType) }
                    var expandedRepeat by remember { mutableStateOf<Boolean>(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedRepeat = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "重复: $repeatType")
                        }
                        DropdownMenu(
                            expanded = expandedRepeat,
                            onDismissRequest = { expandedRepeat = false }
                        ) {
                            RepeatType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.toString()) },
                                    onClick = {
                                        repeatType = type
                                        expandedRepeat = false
                                        if (type == RepeatType.CUSTOM) {
                                            showCustomDaysDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                    
                    // 自定义日期选择对话框
                    if (showCustomDaysDialog) {
                        Dialog(onDismissRequest = { showCustomDaysDialog = false }) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "选择重复日期",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    val weekDays = listOf(
                                        "周一" to 1,
                                        "周二" to 2,
                                        "周三" to 3,
                                        "周四" to 4,
                                        "周五" to 5,
                                        "周六" to 6,
                                        "周日" to 7
                                    )
                                    
                                    weekDays.forEach { (dayName, dayValue) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = selectedCustomDays.contains(dayValue),
                                                onCheckedChange = { checked ->
                                                    selectedCustomDays = if (checked) {
                                                        selectedCustomDays + dayValue
                                                    } else {
                                                        selectedCustomDays - dayValue
                                                    }
                                                }
                                            )
                                            Text(text = dayName)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = { showCustomDaysDialog = false },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(text = "取消")
                                        }
                                        
                                        Button(
                                            onClick = {
                                                showCustomDaysDialog = false
                                            }
                                        ) {
                                            Text(text = "确定")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 关闭方式选择
                    var dismissType by remember { mutableStateOf<DismissType>(alarm.dismissType) }
                    var expandedDismiss by remember { mutableStateOf<Boolean>(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expandedDismiss = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "关闭方式: $dismissType")
                        }
                        DropdownMenu(
                            expanded = expandedDismiss,
                            onDismissRequest = { expandedDismiss = false }
                        ) {
                            DismissType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.toString()) },
                                    onClick = {
                                        dismissType = type
                                        expandedDismiss = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // 保存和取消按钮
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { editingAlarm = null },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(text = "取消")
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        val updatedAlarm = alarm.copy(
                                            hour = currentAlarmTime.get(Calendar.HOUR_OF_DAY),
                                            minute = currentAlarmTime.get(Calendar.MINUTE),
                                            label = label,
                                            repeatType = repeatType,
                                            dismissType = dismissType,
                                            customDays = if (selectedCustomDays.isNotEmpty()) {
                                                selectedCustomDays.joinToString(",")
                                            } else null,
                                            dayOfMonth = if (repeatType == RepeatType.MONTHLY || repeatType == RepeatType.YEARLY) {
                                                currentAlarmTime.get(Calendar.DAY_OF_MONTH)
                                            } else null,
                                            month = if (repeatType == RepeatType.YEARLY) {
                                                currentAlarmTime.get(Calendar.MONTH) + 1
                                            } else null
                                        )
                                        AlarmDatabaseFacade.updateAlarm(activity, updatedAlarm)
                                        AlarmManager.setAlarm(activity, currentAlarmTime, updatedAlarm)
                                        editingAlarm = null
                                    } catch (e: Exception) {
                                        Log.e("AlarmTest", "更新闹钟失败: ${e.message}", e)
                                    }
                                }
                            }
                        ) {
                            Text(text = "保存")
                        }
                    }
                }
            }
        }
    }
}

fun showTimePickerDialog(activity: MainActivity, initialTime: Calendar, onTimeSet: (Calendar) -> Unit) {
    val hour = initialTime.get(Calendar.HOUR_OF_DAY)
    val minute = initialTime.get(Calendar.MINUTE)
    TimePickerDialog(activity, { _, selectedHour, selectedMinute ->
        val selectedTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
            set(Calendar.SECOND, 0)
        }
        onTimeSet(selectedTime)
    }, hour, minute, true).show()
}

private fun formatWeeklyDay(customDays: String?): String {
    if (customDays.isNullOrEmpty()) return ""
    
    val day = customDays.split(",").firstOrNull()?.toIntOrNull() ?: return ""
    val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    
    return dayNames.getOrNull(day - 1) ?: ""
}

private fun formatCustomDays(customDays: String?): String {
    if (customDays.isNullOrEmpty()) return ""
    
    val days = customDays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    
    // 检查是否匹配特殊组合
    when {
        // 周一到周五
        days == setOf(1, 2, 3, 4, 5) -> return "工作日"
        // 周六和周日
        days == setOf(6, 7) -> return "周末"
        // 周一到周日
        days == setOf(1, 2, 3, 4, 5, 6, 7) -> return "每天"
        // 单个日期
        days.size == 1 -> {
            val day = days.first()
            val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            return "每周${dayNames.getOrNull(day - 1) ?: ""}"
        }
        // 其他情况显示自定义组合
        else -> {
            val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            return "每周" + days.sorted().mapNotNull { day ->
                dayNames.getOrNull(day - 1)
            }.joinToString("、")
        }
    }
}

