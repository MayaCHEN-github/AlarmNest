package edu.cuhk.csci3310.csci3310project.alarm

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import edu.cuhk.csci3310.csci3310project.MainActivity
import java.util.*

@Composable
fun AlarmTest(activity: MainActivity, innerPadding: PaddingValues) {
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var alarmTimes by remember { mutableStateOf<List<Calendar>>(emptyList()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            if (AlarmPermission.checkAndRequestPermissions(activity, activity.permissionLauncher)) {
                showTimePickerDialog(activity, currentTime) { selectedTime ->
                    AlarmManager.setAlarm(activity, selectedTime)
                    alarmTimes = (alarmTimes + selectedTime).toList()
                }
            }
        }) {
            Text(text = "设置闹钟")
        }
        Text(text = "当前时间: ${String.format(Locale.getDefault(), "%02d:%02d", currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE))}")
        alarmTimes.forEach { alarmTime ->
            Text(text = "闹钟时间: ${String.format(Locale.getDefault(), "%02d:%02d", alarmTime.get(Calendar.HOUR_OF_DAY), alarmTime.get(Calendar.MINUTE))}")
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

