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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import edu.cuhk.csci3310.csci3310project.alarm.AlarmPermission
import edu.cuhk.csci3310.csci3310project.alarm.AlarmReceiver
import edu.cuhk.csci3310.csci3310project.alarm.AlarmTest
import edu.cuhk.csci3310.csci3310project.alarm.storage.Alarm
import edu.cuhk.csci3310.csci3310project.alarm.storage.AlarmDatabaseFacade
import edu.cuhk.csci3310.csci3310project.screen.AlarmOffScreen
import edu.cuhk.csci3310.csci3310project.screen.AlarmScreen
import edu.cuhk.csci3310.csci3310project.screen.AlarmTypingScreen
import edu.cuhk.csci3310.csci3310project.screen.ClockListScreen
import edu.cuhk.csci3310.csci3310project.screen.viewmodel.ClockListScreenViewModel
import edu.cuhk.csci3310.csci3310project.sensor.StepCounterViewModel
import edu.cuhk.csci3310.csci3310project.sensor.StepCounterUI
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

class MainActivity : ComponentActivity() {
    lateinit var stepCounterViewModel: StepCounterViewModel
    private lateinit var navController: NavHostController
    private val navigationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.cuhk.csci3310.csci3310project.NAVIGATE_TO_ALARM") {
                // 只在特定界面才跳转到 alarm_screen
                val currentRoute = navController.currentDestination?.route
                if (currentRoute == "clock_list_screen") {
                    navController.navigate("alarm_screen") {
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
        
        // 初始化权限请求启动器
        AlarmPermission.initializePermissionLaunchers(this)
        
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
                        navController.navigate("alarm_screen") {
                            popUpTo("clock_list_screen") { inclusive = true }
                        }
                    }
                }
                
                if (AlarmPermission.shouldShowStoragePermissionDialog()) {
                    AlertDialog(
                        onDismissRequest = { AlarmPermission.setShowStoragePermissionDialog(false) },
                        title = { Text("需要存储权限") },
                        text = { Text("此应用需要存储权限来保存闹钟数据。请在设置中授予权限。") },
                        confirmButton = {
                            Button(onClick = {
                                AlarmPermission.setShowStoragePermissionDialog(false)
                                AlarmPermission.openAppSettings(this)
                            }) {
                                Text("去设置")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { AlarmPermission.setShowStoragePermissionDialog(false) }) {
                                Text("取消")
                            }
                        }
                    )
                }
                
                NavHost(
                    navController = navController,
                    startDestination = "clock_list_screen"
                ) {
                    composable("clock_list_screen") {
                        TestScreen(activity = this@MainActivity)
                    }
                    composable("alarm_screen") {
                        val alarmId = intent?.getLongExtra("alarm_id", -1) ?: -1
                        val isSubAlarm = intent?.getBooleanExtra("is_sub_alarm", false) ?: false
                        var alarm by remember { mutableStateOf<Alarm?>(null) }
                        
                        LaunchedEffect(alarmId) {
                            if (alarmId != -1L) {
                                alarm = if (isSubAlarm) {
                                    // 如果是子闹钟，获取父闹钟
                                    val subAlarm = AlarmDatabaseFacade.getSubAlarmById(this@MainActivity, alarmId)
                                    if (subAlarm != null) {
                                        AlarmDatabaseFacade.getAlarmById(this@MainActivity, subAlarm.parentAlarmId)
                                    } else null
                                } else {
                                    AlarmDatabaseFacade.getAlarmById(this@MainActivity, alarmId)
                                }
                            }
                        }
                        
                        AlarmScreen(
                            navController = navController,
                            alarm = alarm,
                            onStartTask = {
                                stopAlarm()
                                navController.navigate("clock_list_screen") {
                                    popUpTo("alarm_screen") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("typing_alarm_screen") {
                        AlarmTypingScreen(navController = navController)
                    }
                    composable("alarm_off_screen") {
                        AlarmOffScreen(
                            navController = navController,
                            onStopAlarm = { stopAlarm() }
                        )
                    }
                }
            }
        }
        
        // 检查并请求权限
        AlarmPermission.checkAndRequestPermissions(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(navigationReceiver)
    }

    override fun onResume() {
        super.onResume()
        stepCounterViewModel.start()
    }

    override fun onPause() {
        super.onPause()
        stepCounterViewModel.stop()
    }

    fun isStoragePermissionGranted(): Boolean {
        return AlarmPermission.isStoragePermissionGranted()
    }

    fun isNotificationPermissionGranted(): Boolean {
        return AlarmPermission.isNotificationPermissionGranted()
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

