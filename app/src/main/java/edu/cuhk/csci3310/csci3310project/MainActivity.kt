package edu.cuhk.csci3310.csci3310project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import edu.cuhk.csci3310.csci3310project.backend.features.alarm.AlarmReceiver
import edu.cuhk.csci3310.csci3310project.frontend.components.AlarmTest
import edu.cuhk.csci3310.csci3310project.frontend.screens.ClockListScreen
import edu.cuhk.csci3310.csci3310project.frontend.viewmodels.ClockListScreenViewModel
import edu.cuhk.csci3310.csci3310project.frontend.components.StepCounterUI
import edu.cuhk.csci3310.csci3310project.frontend.components.StepCounterViewModel
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme
import edu.cuhk.csci3310.csci3310project.utils.PermissionManager

class MainActivity : ComponentActivity() {
    lateinit var stepCounterViewModel: StepCounterViewModel
    private lateinit var navController: NavHostController
    private val navigationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.cuhk.csci3310.csci3310project.NAVIGATE_TO_ALARM") {
                // 只在特定界面才跳转到 alarm_screen
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == "clock_list_screen") {
                    val alarmId = intent.getLongExtra("alarm_id", -1)
                    val isSubAlarm = intent.getBooleanExtra("is_sub_alarm", false)
                    val alarmLabel = intent.getStringExtra("alarm_label") ?: "闹钟提醒"
                    
                    navController.navigate("alarm_screen/${alarmId}/${isSubAlarm}/${alarmLabel}") {
                        popUpTo("clock_list_screen") { inclusive = true }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        
        // 注册广播接收器
        registerReceiver(navigationReceiver, IntentFilter("edu.cuhk.csci3310.csci3310project.NAVIGATE_TO_ALARM"))
        
        // 检查是否需要停止闹钟
        if (intent?.getBooleanExtra("stop_alarm", false) == true) {
            stopAlarm()
        }
        
        // 初始化计步器
        stepCounterViewModel = StepCounterViewModel()
        stepCounterViewModel.initialize(this)
        
        // 初始化权限管理器
        PermissionManager.initialize(this)
        
        enableEdgeToEdge()
        setContent {
            CSCI3310ProjectTheme {
                navController = rememberNavController()
                
                // 处理从通知进入的情况
                LaunchedEffect(Unit) {
                    if (intent?.getStringExtra("navigate_to") == "alarm_screen") {
                        if (intent.getBooleanExtra("send_broadcast", false)) {
                            val broadcastIntent = Intent("edu.cuhk.csci3310.csci3310project.NAVIGATE_TO_ALARM")
                            sendBroadcast(broadcastIntent)
                        }
                        navController.navigate("alarm_screen/${intent.getLongExtra("alarm_id", -1)}/${intent.getBooleanExtra("is_sub_alarm", false)}/${intent.getStringExtra("alarm_label") ?: "闹钟提醒"}") {
                            popUpTo("clock_list_screen") { inclusive = true }
                        }
                    }
                }
                
                // 显示权限请求对话框
                if (PermissionManager.shouldShowPermissionDialog()) {
                    AlertDialog(
                        onDismissRequest = { PermissionManager.onPermissionCancelled(this@MainActivity) },
                        title = { Text(PermissionManager.getPermissionDialogTitle()) },
                        text = { Text(PermissionManager.getPermissionDialogContent()) },
                        confirmButton = {
                            Button(onClick = {
                                PermissionManager.onPermissionConfirmed(this@MainActivity)
                            }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { PermissionManager.onPermissionCancelled(this@MainActivity) }) {
                                Text("取消")
                            }
                        }
                    )
                }
                
                AppNavigation(
                    navController = navController,
                    activity = this@MainActivity,
                    onStopAlarm = { stopAlarm() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(navigationReceiver)
    }

    override fun onResume() {
        super.onResume()
        // 检查当前权限状态
        PermissionManager.checkPermissionStatus(this)
        // 开始权限请求流程
        PermissionManager.checkAndRequestPermissions(this)
        
        if (PermissionManager.isActivityRecognitionPermissionGranted()) {
            stepCounterViewModel.start()
        }
    }

    override fun onPause() {
        super.onPause()
        stepCounterViewModel.stop()
    }

    fun isStoragePermissionGranted(): Boolean {
        return PermissionManager.isStoragePermissionGranted()
    }

    fun isNotificationPermissionGranted(): Boolean {
        return PermissionManager.isNotificationPermissionGranted()
    }

    private fun stopAlarm() {
        val stopIntent = Intent(this, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_STOP_ALARM
        }
        sendBroadcast(stopIntent)
    }
}

@Composable
fun MainScreen(activity: MainActivity) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center
        ) {
            StepCounterUI(activity.stepCounterViewModel)
            Spacer(modifier = Modifier.height(32.dp))
            AlarmTest(activity, innerPadding)
        }
    }
}

@Composable
fun TestScreen(activity: MainActivity){
    val viewModel: ClockListScreenViewModel = viewModel(
        factory = ClockListScreenViewModel.Factory(activity)
    )
    ClockListScreen(viewModel = viewModel)
}

