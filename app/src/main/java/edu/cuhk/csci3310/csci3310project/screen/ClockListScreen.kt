package edu.cuhk.csci3310.csci3310project.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.cuhk.csci3310.csci3310project.screen.model.AlarmData
import edu.cuhk.csci3310.csci3310project.screen.model.SubAlarmData
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.ClockItem
import edu.cuhk.csci3310.csci3310project.screen.uiComponent.NextAlarmText
import edu.cuhk.csci3310.csci3310project.screen.viewmodel.ClockListScreenViewModel
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

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
