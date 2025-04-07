package edu.cuhk.csci3310.csci3310project.frontend.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import edu.cuhk.csci3310.csci3310project.R
import edu.cuhk.csci3310.csci3310project.backend.data.RepeatType
import edu.cuhk.csci3310.csci3310project.backend.data.SubAlarmData
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import kotlin.math.absoluteValue

private const val TAG = "ClockList"

// 显示下一个主闹钟的文本组件。
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

// 显示星期几的行组件。
@Composable
fun WeekdayRow(
    selectedDays: List<Boolean> = List(7) { false },
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled && onClick != null) { onClick?.invoke() },
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

// 显示时间选择对话框的组件(用于TimeDisplay组件)。
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
    initialHour: Int = 0,
    initialMinute: Int = 0
) {
    val timeState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    Dialog(onDismissRequest = onDismiss) { // 显示时间选择对话框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                
                TimePicker( // time picker
                    state = timeState
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { // 取消按钮
                        Text("Cancel", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(timeState.hour, timeState.minute)
                            onDismiss()
                        }
                    ) {                             // 确认按钮
                        Text("Confirm", color = Color.Black)
                    }
                }
            }
        }
    }
}

// 时间显示组件。
@Composable
fun TimeDisplay(
    time: String,
    enabled: Boolean = true,
    onTimeChange: ((Int, Int) -> Unit)? = null
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Text( // 时间显示
        text = time,
        fontSize = 48.sp,
        fontWeight = FontWeight.Normal,
        color = if (enabled) Color.White else Color.Gray,
        modifier = Modifier
            .clickable(enabled = enabled && onTimeChange != null) {
                showTimePicker = true // 显示时间选择对话框
            }
    )

    if (showTimePicker) {
        val (hour, minute) = time.split(":").map { it.toInt() }
        TimePickerDialog( // 显示时间选择对话框
            onDismiss = { showTimePicker = false },
            onConfirm = { newHour, newMinute ->
                onTimeChange?.invoke(newHour, newMinute)
            },
            initialHour = hour,
            initialMinute = minute
        )
    }
}

// 显示唤醒方式的单个图标（用于ActionIcons组件）。
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

// 显示唤醒方式的图标组件。
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
            contentDescription = "Typing alarm" // 输入唤醒
        )
        ActionIcon(
            iconRes = R.drawable.list_24px,
            enabled = enabled,
            contentDescription = "CheckList alarm" // 清单唤醒
        )
        ActionIcon(
            iconRes = R.drawable.directions_walk_24px,
            enabled = enabled,
            contentDescription = "Walking alarm" // 步行唤醒
        )
    }
}

// 显示闹钟lebel/memo调整的对话框组件（用于AlarmDescription组件）。
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmDescriptionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialDescription: String = ""
) {
    var description by remember { mutableStateOf(initialDescription) }

    Dialog(onDismissRequest = onDismiss) { // 对话框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit Description",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField( // 输入框
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black) // 取消按钮
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(description)
                            onDismiss()
                        }
                    ) {
                        Text("Confirm", color = Color.Black) // 确认按钮
                    }
                }
            }
        }
    }
}

// 显示闹钟lebel/memo的组件。
@Composable
fun AlarmDescription(
    description: String,
    enabled: Boolean = true,
    onDescriptionChange: ((String) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier
                .weight(1f)
                .clickable(enabled = enabled && onDescriptionChange != null) {
                    showDialog = true // 单击时显示对话框
                }
        )
        ActionIcons(enabled = enabled) // 显示唤醒方式的图标组件
    }

    if (showDialog) {
        AlarmDescriptionDialog(
            onDismiss = { showDialog = false },
            onConfirm = { newDescription ->
                onDescriptionChange?.invoke(newDescription)
            },
            initialDescription = description
        )
    }
}

// 显示闹钟图标组件。
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

// 显示删除闹钟的对话框组件（用于DeleteIcon组件）。
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Delete Alarm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Are you sure you want to delete this alarm?", 
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { 
                        Text("Cancel", color = Color.Black) // 取消按钮
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton( 
                        onClick = {
                            onConfirm()
                            onDismiss()
                        }
                    ) {
                        Text("Delete", color = Color.Red) // 删除按钮
                    }
                }
            }
        }
    }
}

// 显示删除闹钟的图标组件
@Composable
fun DeleteIcon(
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Icon(
        painter = painterResource(id = R.drawable.delete_24px),
        contentDescription = "Delete",
        tint = if (enabled) Color.White else Color.Gray,
        modifier = Modifier.clickable(enabled = enabled) { 
            if (enabled) {
                showDeleteDialog = true
            }
        }
    )

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = onClick
        )
    }
}

// 显示时间差对话框组件（用于SubAlarmEditDialog组件）。
@Composable
fun TimeDiffDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    initialTimeDiffMinutes: Int = 0
) {
    var timeDiffMinutes by remember { mutableStateOf(initialTimeDiffMinutes) }
    val hours = timeDiffMinutes / 60
    val minutes = timeDiffMinutes % 60

    Dialog(onDismissRequest = onDismiss) { // 对话框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Set Time Difference",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 时间差显示
                Text(
                    text = "${if (timeDiffMinutes >= 0) "+" else "-"}${hours.absoluteValue}:${String.format("%02d", minutes.absoluteValue)}",
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 小时hours滑块
                Text("Hours", color = Color.Black)
                Slider( // 使用slider以避免意外输入！
                    value = hours.toFloat(),
                    onValueChange = { newValue ->
                        val newHours = newValue.toInt()
                        timeDiffMinutes = newHours * 60 + minutes
                    },
                    valueRange = -23f..23f,
                    steps = 46
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 分钟minutes滑块
                Text("Minutes", color = Color.Black)
                Slider(
                    value = minutes.toFloat(),
                    onValueChange = { newValue ->
                        val newMinutes = newValue.toInt()
                        timeDiffMinutes = hours * 60 + newMinutes
                    },
                    valueRange = -59f..59f,
                    steps = 118
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black) // 取消按钮
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(timeDiffMinutes)
                            onDismiss()
                        }
                    ) {
                        Text("Confirm", color = Color.Black) // 确认按钮
                    }
                }
            }
        }
    }
}

// 显示子闹钟编辑对话框组件（用于SubAlarmItem组件）。
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubAlarmEditDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?) -> Unit,
    initialDescription: String = "",
    initialTriggerType: String? = null
) {
    var description by remember(initialDescription) { mutableStateOf(initialDescription) }
    var selectedTriggerType by remember { mutableStateOf(initialTriggerType) }

    // 初始化日志
    Log.d(TAG, "对话框初始化 - initialDescription: $initialDescription, initialTriggerType: $initialTriggerType")
    Log.d(TAG, "当前状态 - description: $description, selectedTriggerType: $selectedTriggerType")
 
    Dialog(onDismissRequest = onDismiss) { // 对话框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Edit Sub-Alarm",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField( // label输入框
                    value = description,
                    onValueChange = { 
                        Log.d(TAG, "label输入变化 - 旧值: $description, 新值: $it")
                        description = it 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Note") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 触发方式选择
                Text("Trigger Type", color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 键盘输入
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.keyboard_24px),
                            contentDescription = "Typing alarm",
                            tint = if (selectedTriggerType == "keyboard") Color.Black else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { 
                                    Log.d(TAG, "选择触发方式 - keyboard")
                                    selectedTriggerType = "keyboard" 
                                }
                        )
                        Text("Typing", color = if (selectedTriggerType == "keyboard") Color.Black else Color.Gray)
                    }
                    
                    // 清单
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.list_24px),
                            contentDescription = "Checklist",
                            tint = if (selectedTriggerType == "list") Color.Black else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { 
                                    Log.d(TAG, "选择触发方式 - list")
                                    selectedTriggerType = "list" 
                                }
                        )
                        Text("Checklist", color = if (selectedTriggerType == "list") Color.Black else Color.Gray)
                    }
                    
                    // 走路
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.directions_walk_24px),
                            contentDescription = "Walking",
                            tint = if (selectedTriggerType == "walk") Color.Black else Color.Gray,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { 
                                    Log.d(TAG, "选择触发方式 - walk")
                                    selectedTriggerType = "walk" 
                                }
                        )
                        Text("Walking", color = if (selectedTriggerType == "walk") Color.Black else Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black) // 取消按钮
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            Log.d(TAG, "确认按钮点击 - 最终值: description=$description, triggerType=$selectedTriggerType")
                            onConfirm(description, selectedTriggerType)
                            onDismiss()
                        }
                    ) {
                        Text("Confirm", color = Color.Black) // 确认按钮
                    }
                }
            }
        }
    }
}

// 显示子闹钟项组件(单个子闹钟)。
@Composable
fun SubAlarmItem(
    subAlarm: SubAlarmData,
    enabled: Boolean = true,
    onDelete: () -> Unit = {},
    onTimeDiffChange: (Int) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onTriggerTypeChange: (String?) -> Unit = {}
) {
    var showTimeDiffDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentDescription by remember(subAlarm) { mutableStateOf(subAlarm.description) }
    var currentTriggerType by remember(subAlarm) { mutableStateOf(subAlarm.triggerType) }
    var currentTimeDiff by remember(subAlarm) { mutableStateOf(subAlarm.timeDiff) }

    // 日志debug
    Log.d(TAG, "子闹钟项渲染 - subAlarm: $subAlarm")
    Log.d(TAG, "当前状态 - description: $currentDescription, triggerType: $currentTriggerType, timeDiff: $currentTimeDiff")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 时间差显示，单击打开时间差对话框
        Text(
            text = currentTimeDiff,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.clickable(enabled = enabled) {
                Log.d(TAG, "点击时间差 - 打开时间差对话框")
                showTimeDiffDialog = true
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 描述显示，单击打开编辑对话框
        Text(
            text = currentDescription,
            fontSize = 14.sp,
            color = if (enabled) Color.White else Color.Gray,
            modifier = Modifier.clickable(enabled = enabled) {
                Log.d(TAG, "点击描述 - 打开编辑对话框")
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 触发方式图标（如果有就显示！）
        if (currentTriggerType != null && enabled) {
            val iconRes = when (currentTriggerType) {
                "keyboard" -> R.drawable.keyboard_24px
                "list" -> R.drawable.list_24px
                "walk" -> R.drawable.directions_walk_24px
                else -> null
            }
            if (iconRes != null) {
                ActionIcon(
                    iconRes = iconRes,
                    enabled = enabled,
                    contentDescription = "Trigger type $currentTriggerType"
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // 闹钟状态图标和删除图标
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeleteIcon(
                enabled = subAlarm.enabled,
                onClick = onDelete
            )
            AlarmIcon(enabled = subAlarm.enabled && enabled)
        }
    }

    if (showTimeDiffDialog) {
        // 解析当前时间差为分钟数
        val currentTimeDiffMinutes = currentTimeDiff.let { diff ->
            val sign = if (diff.startsWith("+")) 1 else -1
            val parts = diff.substring(1).split(":")
            val hours = parts[0].toInt()
            val minutes = if (parts.size > 1) parts[1].toInt() else 0
            sign * (hours * 60 + minutes)
        }

        TimeDiffDialog(
            onDismiss = { 
                Log.d(TAG, "时间差对话框关闭")
                showTimeDiffDialog = false 
            },
            onConfirm = { newTimeDiffMinutes ->
                Log.d(TAG, "时间差对话框确认 - 新时间差: $newTimeDiffMinutes")
                // 更新本地状态
                val hours = newTimeDiffMinutes / 60
                val minutes = newTimeDiffMinutes % 60
                currentTimeDiff = "${if (newTimeDiffMinutes >= 0) "+" else ""}${hours}:${String.format("%02d", minutes.absoluteValue)}"
                // 调用回调
                onTimeDiffChange(newTimeDiffMinutes)
            },
            initialTimeDiffMinutes = currentTimeDiffMinutes
        )
    }

    if (showEditDialog) {
        SubAlarmEditDialog(
            onDismiss = { 
                Log.d(TAG, "编辑对话框关闭")
                showEditDialog = false 
            },
            onConfirm = { newDescription, newTriggerType ->
                Log.d(TAG, "编辑对话框确认 - 新描述: $newDescription, 新触发方式: $newTriggerType")
                // 更新本地状态
                currentDescription = newDescription
                currentTriggerType = newTriggerType
                // 调用回调
                onDescriptionChange(newDescription)
                onTriggerTypeChange(newTriggerType)
            },
            initialDescription = currentDescription,
            initialTriggerType = currentTriggerType
        )
    }
}

@Composable
fun SubAlarmList(
    subAlarms: List<SubAlarmData>,
    enabled: Boolean = true,
    onSubAlarmDelete: (SubAlarmData) -> Unit = {},
    onSubAlarmTimeDiffChange: (SubAlarmData, Int) -> Unit = { _, _ -> },
    onSubAlarmDescriptionChange: (SubAlarmData, String) -> Unit = { _, _ -> },
    onSubAlarmTriggerTypeChange: (SubAlarmData, String?) -> Unit = { _, _ -> },
    onAddSubAlarm: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        subAlarms.forEach { subAlarm ->
            SubAlarmItem(
                subAlarm = subAlarm,
                enabled = enabled,
                onDelete = { onSubAlarmDelete(subAlarm) },
                onTimeDiffChange = { newTimeDiffMinutes ->
                    onSubAlarmTimeDiffChange(subAlarm, newTimeDiffMinutes)
                },
                onDescriptionChange = { newDescription ->
                    onSubAlarmDescriptionChange(subAlarm, newDescription)
                },
                onTriggerTypeChange = { newTriggerType ->
                    onSubAlarmTriggerTypeChange(subAlarm, newTriggerType)
                }
            )
        }
        
        // 添加新子闹钟按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Add new subAlarm",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        onAddSubAlarm()
                    }
            )
        }
    }
}

@Composable
fun WeekdaySelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    initialCustomDays: String? = null
) {
    // 根据传入的customDays初始化selectedDays
    var selectedDays by remember(initialCustomDays) {
        mutableStateOf(
            if (initialCustomDays != null) {
                val days = initialCustomDays.split(",").mapNotNull { it.toIntOrNull() }
                List(7) { index ->
                    val dayNumber = if (index == 0) 7 else index
                    dayNumber in days
                }
            } else {
                List(7) { false }
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Repeat Days",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 星期选项
                listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday").forEachIndexed { index, day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selectedDays[index],
                            onCheckedChange = { checked ->
                                selectedDays = selectedDays.toMutableList().apply {
                                    set(index, checked)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = day,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 确认和取消按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            // 将选中的星期转换为字符串,如"1,2,3"表示周一、周二、周三
                            val selectedDayNumbers = selectedDays
                                .mapIndexed { index, selected -> if (selected) index + 1 else null }
                                .filterNotNull()
                                .joinToString(",")
                            if (selectedDayNumbers.isNotEmpty()) {
                                onConfirm(selectedDayNumbers)
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Confirm", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun RepeatTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (RepeatType, String?) -> Unit,
    initialRepeatType: RepeatType = RepeatType.ONCE,
    initialCustomDays: String? = null
) {
    var selectedType by remember(initialRepeatType) { mutableStateOf(initialRepeatType) }
    var showWeekdayDialog by remember { mutableStateOf(false) }

    // 星期选择对话框
    if (showWeekdayDialog) {
        WeekdaySelectionDialog(
            onDismiss = { showWeekdayDialog = false },
            onConfirm = { days ->
                onConfirm(RepeatType.CUSTOM, days)
                onDismiss()
            },
            initialCustomDays = initialCustomDays
        )
    }

    // 主对话框
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select Repeat Mode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 重复模式选项
                listOf(
                    RepeatType.ONCE,
                    RepeatType.DAILY,
                    RepeatType.WEEKDAYS,
                    RepeatType.WEEKENDS,
                    RepeatType.CUSTOM
                ).forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                if (type == RepeatType.CUSTOM) {
                                    showWeekdayDialog = true
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color.Black,
                                unselectedColor = Color.Gray
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when(type) {
                                RepeatType.ONCE -> "Once"
                                RepeatType.DAILY -> "Daily"
                                RepeatType.WEEKDAYS -> "Weekdays"
                                RepeatType.WEEKENDS -> "Weekends"
                                RepeatType.CUSTOM -> "Custom"
                                else -> ""
                            },
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 确认和取消按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            if (selectedType == RepeatType.CUSTOM) {
                                showWeekdayDialog = true
                            } else {
                                onConfirm(selectedType, null)
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Confirm", color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ClockItem(
    time: String = "08:00",
    description: String = "Wake-up alarm",
    initialEnabled: Boolean = true,
    subAlarms: List<SubAlarmData> = emptyList(),
    repeatType: RepeatType = RepeatType.ONCE,
    customDays: String? = null,
    onTimeChange: ((Int, Int) -> Unit)? = null,
    onToggleEnabled: (Boolean) -> Unit = {},
    onRepeatTypeChanged: (RepeatType, String?) -> Unit = { _, _ -> },
    onDescriptionChange: ((String) -> Unit)? = null,
    onDelete: () -> Unit = {},
    onSubAlarmDelete: (SubAlarmData) -> Unit = {},
    onSubAlarmTimeDiffChange: (SubAlarmData, Int) -> Unit = { _, _ -> },
    onSubAlarmDescriptionChange: (SubAlarmData, String) -> Unit = { _, _ -> },
    onSubAlarmTriggerTypeChange: (SubAlarmData, String?) -> Unit = { _, _ -> },
    onAddSubAlarm: () -> Unit = {}
) {
    var isEnabled by remember { mutableStateOf(initialEnabled) }
    var showRepeatDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 根据repeatType和customDays生成selectedDays
    val selectedDays = remember(repeatType, customDays) {
        when (repeatType) {
            RepeatType.ONCE -> List(7) { false }
            RepeatType.DAILY -> List(7) { true }
            RepeatType.WEEKDAYS -> List(7) { index -> index in 1..5 } // 1-5是周一到周五
            RepeatType.WEEKENDS -> List(7) { index -> index == 0 || index == 6 } // 0和6是周六和周日
            RepeatType.CUSTOM -> {
                val days = customDays?.split(",")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                List(7) { index ->
                    // 注意: index从0开始(周日到周六),而days中的数字从1开始(周一到周日)
                    // 所以需要将index转换为对应的星期数字
                    val dayNumber = if (index == 0) 7 else index // 周日是7,周一是1
                    dayNumber in days
                }
            }
            else -> List(7) { false }
        }
    }

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
                enabled = isEnabled,
                onClick = { showRepeatDialog = true }
            )

            // 重复模式选择对话框
            if (showRepeatDialog) {
                RepeatTypeDialog(
                    onDismiss = { showRepeatDialog = false },
                    onConfirm = { repeatType, customDays ->
                        onRepeatTypeChanged.invoke(repeatType, customDays)
                    },
                    initialRepeatType = repeatType,
                    initialCustomDays = customDays
                )
            }

            // 时间和开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeDisplay(
                    time = time,
                    enabled = isEnabled,
                    onTimeChange = onTimeChange
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.delete_24px),
                            contentDescription = "Delete",
                            tint = if (isEnabled) Color.White else Color.Gray,
                            modifier = Modifier.clickable(enabled = isEnabled) { 
                                if (isEnabled) {
                                    showDeleteDialog = true
                                }
                            }
                        )
                        AlarmIcon(enabled = isEnabled)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { newValue ->
                            isEnabled = newValue
                            onToggleEnabled.invoke(newValue)
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
                enabled = isEnabled,
                onDescriptionChange = onDescriptionChange
            )
            
            // 子闹钟列表 - 始终显示
            SubAlarmList(
                subAlarms = subAlarms,
                enabled = isEnabled,
                onSubAlarmDelete = onSubAlarmDelete,
                onSubAlarmTimeDiffChange = onSubAlarmTimeDiffChange,
                onSubAlarmDescriptionChange = onSubAlarmDescriptionChange,
                onSubAlarmTriggerTypeChange = onSubAlarmTriggerTypeChange,
                onAddSubAlarm = onAddSubAlarm
            )
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = onDelete
        )
    }
}
