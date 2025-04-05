package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import edu.cuhk.csci3310.csci3310project.screen.viewmodel.ClockListScreenViewModel
import edu.cuhk.csci3310.csci3310project.screen.viewmodel.ClockListUiState
import edu.cuhk.csci3310.csci3310project.screen.model.AlarmData
import edu.cuhk.csci3310.csci3310project.screen.model.SubAlarmData
import androidx.compose.foundation.shape.CircleShape

@Composable
fun NextAlarmText(
    timeUntilNextAlarm: String
) {
    Text(
        text = timeUntilNextAlarm,
        fontSize = 18.sp,
        color = Color.White,
        modifier = Modifier.padding(16.dp)
    )
}

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
                            onToggleEnabled = { isEnabled ->
                                // TODO: 调用ViewModel的toggleAlarmEnabled方法
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekdayRow(
    selectedDays: List<Boolean> = List(7) { false },
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        val days = listOf("S","M","T","W","T","F","S")
        days.forEachIndexed { index, day ->
            Text(
                text = day,
                fontSize = 12.sp,
                color = if (!enabled) Color.Gray else if (selectedDays[index]) Color.White else Color.Gray
            )
            if (index < days.size - 1) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun TimeDisplay(
    time: String,
    enabled: Boolean = true
) {
    Text(
        text = time,
        fontSize = 48.sp,
        fontWeight = FontWeight.Normal,
        color = if (enabled) Color.White else Color.Gray
    )
}

@Composable
fun ActionIcon(
    iconRes: Int,
    enabled: Boolean = true,
    contentDescription: String
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        tint = if (enabled) Color.White else Color.Gray,
        modifier = Modifier.size(16.dp)
    )
}

@Composable
fun ActionIcons(
    enabled: Boolean = true
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        ActionIcon(
            iconRes = R.drawable.keyboard_24px,
            enabled = enabled,
            contentDescription = "Typing alarm"
        )
        ActionIcon(
            iconRes = R.drawable.list_24px,
            enabled = enabled,
            contentDescription = "CheckList alarm"
        )
        ActionIcon(
            iconRes = R.drawable.directions_walk_24px,
            enabled = enabled,
            contentDescription = "Walking alarm"
        )
    }
}

@Composable
fun AlarmDescription(
    description: String,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray
        )
        ActionIcons(enabled = enabled)
    }
}

@Composable
fun AlarmIcon(
    enabled: Boolean = true
) {
    Icon(
        painter = painterResource(
            id = if (enabled) R.drawable.alarm_24px else R.drawable.alarm_off_24px
        ),
        contentDescription = if (enabled) "Alarm On" else "Alarm Off",
        tint = if (enabled) Color.White else Color.Gray
    )
}

@Composable
fun SubAlarmItem(
    subAlarm: SubAlarmData,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 时间差
        Text(
            text = subAlarm.timeDiff,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 描述
        Text(
            text = subAlarm.description,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 触发方式图标（如果有）
        if (subAlarm.triggerType != null && enabled) {
            val iconRes = when (subAlarm.triggerType) {
                "keyboard" -> R.drawable.keyboard_24px
                "list" -> R.drawable.list_24px
                "walk" -> R.drawable.alarm_24px // 临时替代
                else -> null
            }
            if (iconRes != null) {
                ActionIcon(
                    iconRes = iconRes,
                    enabled = enabled,
                    contentDescription = "Trigger type ${subAlarm.triggerType}"
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        
        // 闹钟状态图标
        AlarmIcon(enabled = subAlarm.enabled && enabled)
    }
}

@Composable
fun SubAlarmList(
    subAlarms: List<SubAlarmData>,
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        subAlarms.forEach { subAlarm ->
            SubAlarmItem(
                subAlarm = subAlarm,
                enabled = enabled
            )
        }
    }
}

@Composable
fun ClockItem(
    time: String = "08:00",
    description: String = "Wake-up alarm",
    initialEnabled: Boolean = true,
    subAlarms: List<SubAlarmData> = emptyList(),
    onToggleEnabled: ((Boolean) -> Unit)? = null
){
    var isEnabled by remember { mutableStateOf(initialEnabled) }
    val selectedDays = listOf(true, true, true, true, true, false, false)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 星期显示
            WeekdayRow(
                selectedDays = selectedDays,
                enabled = isEnabled
            )
            
            // 时间和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeDisplay(
                    time = time,
                    enabled = isEnabled
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlarmIcon(
                        enabled = isEnabled
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newValue ->
                            isEnabled = newValue
                            onToggleEnabled?.invoke(newValue)
                        },
                        colors = SwitchDefaults.colors(
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.5f),
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
            
            // 闹钟描述
            AlarmDescription(
                description = description,
                enabled = isEnabled
            )
            
            // 子闹钟列表
            if (subAlarms.isNotEmpty()) {
                SubAlarmList(
                    subAlarms = subAlarms,
                    enabled = isEnabled
                )
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
            subAlarms = listOf(
                SubAlarmData("+1:00", "Charge your phone!", "keyboard", true),
                SubAlarmData("+1:20", "Commuting to school", "walk", true)
            )
        ),
        AlarmData("12:30", "Lunch break", false),
        AlarmData("15:00", "Meeting", true),
        AlarmData("18:00", "Gym time", false),
        AlarmData("22:00", "Bedtime", true)
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
                        subAlarms = alarm.subAlarms
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
