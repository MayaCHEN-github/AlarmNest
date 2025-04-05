package edu.cuhk.csci3310.csci3310project.screen

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.alarm.storage.Alarm
import edu.cuhk.csci3310.csci3310project.alarm.storage.AlarmDatabaseFacade
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import java.util.*

@Composable
fun AlarmScreen(
    navController: NavController,
    alarm: Alarm? = null,
    onStartTask: () -> Unit = {}
){
    LaunchedEffect(alarm) {
        Log.d("AlarmScreen", "收到闹钟数据: $alarm")
        Log.d("AlarmScreen", "闹钟memo: ${alarm?.label}")
    }

    CSCI3310ProjectTheme {
        Scaffold () { paddingValues ->
            Column (
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                val calendar = Calendar.getInstance()
                val date = "${calendar.get(Calendar.YEAR)}.${calendar.get(Calendar.MONTH) + 1}.${calendar.get(Calendar.DAY_OF_MONTH)}"
                val dayInWeek = when(calendar.get(Calendar.DAY_OF_WEEK)) {
                    Calendar.MONDAY -> "Monday"
                    Calendar.TUESDAY -> "Tuesday"
                    Calendar.WEDNESDAY -> "Wednesday"
                    Calendar.THURSDAY -> "Thursday"
                    Calendar.FRIDAY -> "Friday"
                    Calendar.SATURDAY -> "Saturday"
                    Calendar.SUNDAY -> "Sunday"
                    else -> ""
                }
                
                // 获取当前路由参数
                val isSubAlarm = navController.currentBackStackEntry?.arguments?.getString("isSubAlarm")?.toBoolean() ?: false
                val alarmId = navController.currentBackStackEntry?.arguments?.getString("alarmId")?.toLongOrNull() ?: -1L
                
                // 处理子闹钟的情况
                var subAlarmInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
                
                LaunchedEffect(Unit) {
                    if (isSubAlarm && alarmId != -1L) {
                        val subAlarm = AlarmDatabaseFacade.getSubAlarmById(navController.context, alarmId)
                        if (subAlarm != null) {
                            // 获取父闹钟
                            val parentAlarm = AlarmDatabaseFacade.getAlarmById(navController.context, subAlarm.parentAlarmId)
                            if (parentAlarm != null) {
                                // 计算子闹钟的实际时间
                                val subAlarmCalendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, parentAlarm.hour)
                                    set(Calendar.MINUTE, parentAlarm.minute)
                                    add(Calendar.MINUTE, subAlarm.timeOffsetMinutes)
                                }
                                val time = String.format("%02d:%02d", 
                                    subAlarmCalendar.get(Calendar.HOUR_OF_DAY),
                                    subAlarmCalendar.get(Calendar.MINUTE))
                                subAlarmInfo = Pair(time, subAlarm.label ?: "No memo")
                            }
                        }
                    }
                }
                
                val time = if (isSubAlarm && subAlarmInfo != null) {
                    subAlarmInfo!!.first
                } else {
                    alarm?.let { 
                        String.format("%02d:%02d", it.hour, it.minute)
                    } ?: String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                }
                
                val memo = if (isSubAlarm && subAlarmInfo != null) {
                    subAlarmInfo!!.second
                } else {
                    alarm?.label?.takeIf { it.isNotBlank() } ?: "No memo"
                }

                Text(
                    text = "$date  $dayInWeek",
                    fontSize = 20.sp
                )

                Text(
                    text = time,
                    fontSize = 70.sp
                )

                Spacer(modifier = Modifier.size(20.dp))

                Image(
                    painter = painterResource(id = R.drawable.alarm_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(130.dp)
                )

                Spacer(modifier = Modifier.size(25.dp))

                Text(
                    text = memo,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.size(15.dp))

                Button(
                    onClick = { 
                        Log.d("AlarmScreen", "点击开始任务按钮")
                        onStartTask()
                        navController.navigate("typing_alarm_screen") 
                    },
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text(
                        text = "START YOUR ALARM TASK!",
                        fontFamily = FontFamily.Default
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmScreenPreview() {
    CSCI3310ProjectTheme {
        AlarmScreen(navController = rememberNavController())
    }
}