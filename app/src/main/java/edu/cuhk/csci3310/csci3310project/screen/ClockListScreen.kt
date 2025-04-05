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
import edu.cuhk.csci3310.csci3310project.alarm.storage.RepeatType
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
                                // TODO: 调用ViewModel的toggleAlarmEnabled方法
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
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun ClockListScreenPreview() {
    val sampleAlarms = listOf(
        AlarmData(
            time = "08:00",
            description = "Wake-up alarm",
            initialEnabled = true,
            repeatType = RepeatType.DAILY,
            customDays = null,
            subAlarms = listOf(
                SubAlarmData("+1:00", "Charge your phone!", "keyboard", true),
                SubAlarmData("+1:20", "Commuting to school", "walk", true)
            )
        ),
        AlarmData(
            time = "12:30",
            description = "Lunch break",
            initialEnabled = false,
            repeatType = RepeatType.WEEKDAYS,
            customDays = null
        ),
        AlarmData(
            time = "15:00",
            description = "Meeting",
            initialEnabled = true,
            repeatType = RepeatType.CUSTOM,
            customDays = "1,3,5" // 周一、周三、周五
        ),
        AlarmData(
            time = "18:00",
            description = "Gym time",
            initialEnabled = false,
            repeatType = RepeatType.WEEKENDS,
            customDays = null
        ),
        AlarmData(
            time = "22:00",
            description = "Bedtime",
            initialEnabled = true,
            repeatType = RepeatType.ONCE,
            customDays = null
        )
    )
    
    CSCI3310ProjectTheme {
        Column {
            NextAlarmText(timeUntilNextAlarm = "1 hour and 30 minutes\ntill the next alarm ...")

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                items(sampleAlarms) { alarm ->
                    ClockItem(
                        time = alarm.time,
                        description = alarm.description,
                        initialEnabled = alarm.initialEnabled,
                        subAlarms = alarm.subAlarms,
                        repeatType = alarm.repeatType,
                        customDays = alarm.customDays
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ClockItemPreview() {
    CSCI3310ProjectTheme {
        Column {
            // 启用状态的闹钟，带有子闹钟
            ClockItem(
                time = "08:00",
                description = "Wake-up alarm",
                initialEnabled = true,
                subAlarms = listOf(
                    SubAlarmData("+1:00", "Charge your phone!", "keyboard", true),
                    SubAlarmData("+1:20", "Commuting to school", "walk", true)
                )
            )
            
            Spacer(modifier = Modifier.padding(16.dp))
            
            // 禁用状态的闹钟，带有子闹钟
            ClockItem(
                time = "12:30",
                description = "Lunch break",
                initialEnabled = false,
                subAlarms = listOf(
                    SubAlarmData("+0:30", "Prepare lunch", "list", true),
                    SubAlarmData("+0:45", "Walk to restaurant", "walk", false)
                )
            )
        }
    }
}
