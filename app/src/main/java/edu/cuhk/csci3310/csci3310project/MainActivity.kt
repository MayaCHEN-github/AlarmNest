package edu.cuhk.csci3310.csci3310project

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
import edu.cuhk.csci3310.csci3310project.alarm.AlarmPermission
import edu.cuhk.csci3310.csci3310project.alarm.AlarmTest
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        
        // 初始化权限请求启动器
        AlarmPermission.initializePermissionLaunchers(this)
        
        enableEdgeToEdge()
        setContent {
            CSCI3310ProjectTheme {
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
                MainScreen(this)
            }
        }
        
        // 检查并请求权限
        AlarmPermission.checkAndRequestPermissions(this)
    }

    fun isStoragePermissionGranted(): Boolean {
        return AlarmPermission.isStoragePermissionGranted()
    }

    fun isNotificationPermissionGranted(): Boolean {
        return AlarmPermission.isNotificationPermissionGranted()
    }
}

@Composable
fun MainScreen(activity: MainActivity) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AlarmTest(activity, innerPadding)
    }
}