package edu.cuhk.csci3310.csci3310project.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cuhk.csci3310.csci3310project.alarm.storage.Alarm
import edu.cuhk.csci3310.csci3310project.alarm.storage.DismissType
import edu.cuhk.csci3310.csci3310project.alarm.storage.RepeatType
import edu.cuhk.csci3310.csci3310project.alarm.storage.SubAlarm
import edu.cuhk.csci3310.csci3310project.screen.model.AlarmData
import edu.cuhk.csci3310.csci3310project.screen.model.SubAlarmData
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.ClockItem
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.NextAlarmText
import edu.cuhk.csci3310.csci3310project.screen.viewmodel.ClockListScreenViewModel
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import java.util.*

@Composable
fun ClockListScreen(
    viewModel: ClockListScreenViewModel,
    onAddAlarmClick: () -> Unit = {}
){
    val uiState by viewModel.uiState.collectAsState()
    
    CSCI3310ProjectTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddAlarmClick,
                    containerColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(96.dp)
                        .padding(end = 24.dp, bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add new alarm",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // 显示加载状态
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
                
                // 显示错误信息
                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                // 顶部文本
                NextAlarmText(timeUntilNextAlarm = uiState.timeUntilNextAlarm)
                
                // 添加测试数据按钮
                Button(
                    onClick = { viewModel.createTestData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("创建测试数据")
                }
                
                // 闹钟列表
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.alarms) { alarm ->
                        ClockItem(
                            time = alarm.time,
                            description = alarm.description,
                            initialEnabled = alarm.initialEnabled,
                            subAlarms = alarm.subAlarms,
                            repeatType = alarm.repeatType,
                            customDays = alarm.customDays,
                            onToggleEnabled = { isEnabled ->
                                Log.d("ClockListScreen", "收到开关状态更改请求: isEnabled=$isEnabled")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                viewModel.toggleAlarmEnabled(
                                    Alarm(
                                        id = alarm.id,
                                        hour = alarm.time.split(":")[0].toInt(),
                                        minute = alarm.time.split(":")[1].toInt(),
                                        repeatType = alarm.repeatType,
                                        customDays = alarm.customDays,
                                        label = alarm.description,
                                        isEnabled = alarm.initialEnabled
                                    )
                                )
                            },
                            onRepeatTypeChanged = { repeatType, customDays ->
                                Log.d("ClockListScreen", "收到重复模式更改请求: repeatType=$repeatType, customDays=$customDays")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                viewModel.updateAlarmRepeatType(
                                    alarm = Alarm(
                                        id = alarm.id,
                                        hour = alarm.time.split(":")[0].toInt(),
                                        minute = alarm.time.split(":")[1].toInt(),
                                        repeatType = repeatType,
                                        customDays = customDays,
                                        label = alarm.description,
                                        isEnabled = alarm.initialEnabled
                                    ),
                                    repeatType = repeatType,
                                    customDays = customDays
                                )
                            },
                            onTimeChange = { newHour, newMinute ->
                                Log.d("ClockListScreen", "收到时间更改请求: hour=$newHour, minute=$newMinute")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                viewModel.updateAlarmTime(
                                    alarm = Alarm(
                                        id = alarm.id,
                                        hour = alarm.time.split(":")[0].toInt(),
                                        minute = alarm.time.split(":")[1].toInt(),
                                        repeatType = alarm.repeatType,
                                        customDays = alarm.customDays,
                                        label = alarm.description,
                                        isEnabled = alarm.initialEnabled
                                    ),
                                    hour = newHour,
                                    minute = newMinute
                                )
                            },
                            onDescriptionChange = { newDescription ->
                                Log.d("ClockListScreen", "收到描述更改请求: description=$newDescription")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                viewModel.updateAlarmDescription(
                                    alarm = Alarm(
                                        id = alarm.id,
                                        hour = alarm.time.split(":")[0].toInt(),
                                        minute = alarm.time.split(":")[1].toInt(),
                                        repeatType = alarm.repeatType,
                                        customDays = alarm.customDays,
                                        label = alarm.description,
                                        isEnabled = alarm.initialEnabled
                                    ),
                                    description = newDescription
                                )
                            },
                            onDelete = {
                                Log.d("ClockListScreen", "收到删除请求")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                viewModel.deleteAlarm(
                                    Alarm(
                                        id = alarm.id,
                                        hour = alarm.time.split(":")[0].toInt(),
                                        minute = alarm.time.split(":")[1].toInt(),
                                        repeatType = alarm.repeatType,
                                        customDays = alarm.customDays,
                                        label = alarm.description,
                                        isEnabled = alarm.initialEnabled
                                    )
                                )
                            },
                            onSubAlarmDelete = { subAlarm ->
                                Log.d("ClockListScreen", "收到子闹钟删除请求")
                                Log.d("ClockListScreen", "当前子闹钟数据: $subAlarm")
                                
                                // 从数据库获取完整的Alarm和SubAlarm对象
                                val parentAlarm = Alarm(
                                    id = alarm.id,
                                    hour = alarm.time.split(":")[0].toInt(),
                                    minute = alarm.time.split(":")[1].toInt(),
                                    repeatType = alarm.repeatType,
                                    customDays = alarm.customDays,
                                    label = alarm.description,
                                    isEnabled = alarm.initialEnabled
                                )
                                
                                // 解析timeDiff为timeOffsetMinutes
                                val timeOffsetMinutes = subAlarm.timeDiff.let { diff ->
                                    val sign = if (diff.startsWith("+")) 1 else -1
                                    val parts = diff.substring(1).split(":")
                                    val hours = parts[0].toInt()
                                    val minutes = if (parts.size > 1) parts[1].toInt() else 0
                                    sign * (hours * 60 + minutes)
                                }
                                
                                val subAlarmEntity = SubAlarm(
                                    id = subAlarm.id,  // 直接使用SubAlarmData中的id
                                    parentAlarmId = alarm.id,
                                    timeOffsetMinutes = timeOffsetMinutes,
                                    dismissType = when (subAlarm.triggerType) {
                                        "keyboard" -> DismissType.TEXT_ALARM
                                        "list" -> DismissType.CHECKLIST_ALARM
                                        "walk" -> DismissType.WALK_ALARM
                                        else -> DismissType.NO_ALARM
                                    },
                                    label = subAlarm.description,
                                    isEnabled = subAlarm.enabled
                                )
                                
                                viewModel.deleteSubAlarm(parentAlarm, subAlarmEntity)
                            },
                            onSubAlarmTimeDiffChange = { subAlarm, newTimeDiffMinutes ->
                                Log.d("ClockListScreen", "收到子闹钟时间差更改请求")
                                Log.d("ClockListScreen", "当前子闹钟数据: $subAlarm")
                                Log.d("ClockListScreen", "新的时间差(分钟): $newTimeDiffMinutes")
                                
                                // 从数据库获取完整的Alarm和SubAlarm对象
                                val parentAlarm = Alarm(
                                    id = alarm.id,
                                    hour = alarm.time.split(":")[0].toInt(),
                                    minute = alarm.time.split(":")[1].toInt(),
                                    repeatType = alarm.repeatType,
                                    customDays = alarm.customDays,
                                    label = alarm.description,
                                    isEnabled = alarm.initialEnabled
                                )
                                
                                // 解析timeDiff为timeOffsetMinutes
                                val currentTimeOffsetMinutes = subAlarm.timeDiff.let { diff ->
                                    val sign = if (diff.startsWith("+")) 1 else -1
                                    val parts = diff.substring(1).split(":")
                                    val hours = parts[0].toInt()
                                    val minutes = if (parts.size > 1) parts[1].toInt() else 0
                                    sign * (hours * 60 + minutes)
                                }
                                
                                val subAlarmEntity = SubAlarm(
                                    id = subAlarm.id,  // 直接使用SubAlarmData中的id
                                    parentAlarmId = alarm.id,
                                    timeOffsetMinutes = currentTimeOffsetMinutes,
                                    dismissType = when (subAlarm.triggerType) {
                                        "keyboard" -> DismissType.TEXT_ALARM
                                        "list" -> DismissType.CHECKLIST_ALARM
                                        "walk" -> DismissType.WALK_ALARM
                                        else -> DismissType.NO_ALARM
                                    },
                                    label = subAlarm.description,
                                    isEnabled = subAlarm.enabled
                                )
                                
                                viewModel.updateSubAlarmTimeDiff(parentAlarm, subAlarmEntity, newTimeDiffMinutes)
                            },
                            onSubAlarmDescriptionChange = { subAlarm, newDescription ->
                                Log.d("ClockListScreen", "收到子闹钟备注更改请求")
                                Log.d("ClockListScreen", "当前子闹钟数据: $subAlarm")
                                Log.d("ClockListScreen", "新的备注: $newDescription")
                                
                                // 从数据库获取完整的Alarm和SubAlarm对象
                                val parentAlarm = Alarm(
                                    id = alarm.id,
                                    hour = alarm.time.split(":")[0].toInt(),
                                    minute = alarm.time.split(":")[1].toInt(),
                                    repeatType = alarm.repeatType,
                                    customDays = alarm.customDays,
                                    label = alarm.description,
                                    isEnabled = alarm.initialEnabled
                                )
                                
                                // 解析timeDiff为timeOffsetMinutes
                                val timeOffsetMinutes = subAlarm.timeDiff.let { diff ->
                                    val sign = if (diff.startsWith("+")) 1 else -1
                                    val parts = diff.substring(1).split(":")
                                    val hours = parts[0].toInt()
                                    val minutes = if (parts.size > 1) parts[1].toInt() else 0
                                    sign * (hours * 60 + minutes)
                                }
                                
                                val subAlarmEntity = SubAlarm(
                                    id = subAlarm.id,
                                    parentAlarmId = alarm.id,
                                    timeOffsetMinutes = timeOffsetMinutes,
                                    dismissType = when (subAlarm.triggerType) {
                                        "keyboard" -> DismissType.TEXT_ALARM
                                        "list" -> DismissType.CHECKLIST_ALARM
                                        "walk" -> DismissType.WALK_ALARM
                                        else -> DismissType.NO_ALARM
                                    },
                                    label = subAlarm.description,
                                    isEnabled = subAlarm.enabled
                                )
                                
                                viewModel.updateSubAlarmDescription(parentAlarm, subAlarmEntity, newDescription)
                            },
                            onSubAlarmTriggerTypeChange = { subAlarm, newTriggerType ->
                                Log.d("ClockListScreen", "收到子闹钟触发方式更改请求")
                                Log.d("ClockListScreen", "当前子闹钟数据: $subAlarm")
                                Log.d("ClockListScreen", "新的触发方式: $newTriggerType")
                                
                                // 从数据库获取完整的Alarm和SubAlarm对象
                                val parentAlarm = Alarm(
                                    id = alarm.id,
                                    hour = alarm.time.split(":")[0].toInt(),
                                    minute = alarm.time.split(":")[1].toInt(),
                                    repeatType = alarm.repeatType,
                                    customDays = alarm.customDays,
                                    label = alarm.description,
                                    isEnabled = alarm.initialEnabled
                                )
                                
                                // 解析timeDiff为timeOffsetMinutes
                                val timeOffsetMinutes = subAlarm.timeDiff.let { diff ->
                                    val sign = if (diff.startsWith("+")) 1 else -1
                                    val parts = diff.substring(1).split(":")
                                    val hours = parts[0].toInt()
                                    val minutes = if (parts.size > 1) parts[1].toInt() else 0
                                    sign * (hours * 60 + minutes)
                                }
                                
                                val subAlarmEntity = SubAlarm(
                                    id = subAlarm.id,
                                    parentAlarmId = alarm.id,
                                    timeOffsetMinutes = timeOffsetMinutes,
                                    dismissType = when (subAlarm.triggerType) {
                                        "keyboard" -> DismissType.TEXT_ALARM
                                        "list" -> DismissType.CHECKLIST_ALARM
                                        "walk" -> DismissType.WALK_ALARM
                                        else -> DismissType.NO_ALARM
                                    },
                                    label = subAlarm.description,
                                    isEnabled = subAlarm.enabled
                                )
                                
                                viewModel.updateSubAlarmTriggerType(parentAlarm, subAlarmEntity, newTriggerType)
                            },
                            onAddSubAlarm = {
                                Log.d("ClockListScreen", "收到添加子闹钟请求")
                                Log.d("ClockListScreen", "当前闹钟数据: $alarm")
                                
                                // 从数据库获取完整的Alarm对象
                                val parentAlarm = Alarm(
                                    id = alarm.id,
                                    hour = alarm.time.split(":")[0].toInt(),
                                    minute = alarm.time.split(":")[1].toInt(),
                                    repeatType = alarm.repeatType,
                                    customDays = alarm.customDays,
                                    label = alarm.description,
                                    isEnabled = alarm.initialEnabled
                                )
                                
                                // 创建新的子闹钟
                                val newSubAlarm = SubAlarm(
                                    id = 0, // 新子闹钟的ID将由数据库自动生成
                                    parentAlarmId = alarm.id,
                                    timeOffsetMinutes = 30, // 默认时间差为30分钟
                                    dismissType = DismissType.NO_ALARM, // 默认无触发方式
                                    label = "new subalarm", // 默认描述
                                    isEnabled = true // 默认启用
                                )
                                
                                viewModel.addSubAlarm(parentAlarm, newSubAlarm)
                            }
                        )
                    }
                }
            }
        }
    }
}
