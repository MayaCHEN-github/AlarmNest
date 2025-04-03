package edu.cuhk.csci3310.csci3310project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import edu.cuhk.csci3310.csci3310project.alarm.AlarmTest
import edu.cuhk.csci3310.csci3310project.ui.theme.CSCI3310ProjectTheme

class MainActivity : ComponentActivity() {
    lateinit var permissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "通知权限已授予")
            } else {
                Log.w("MainActivity", "通知权限被拒绝")
            }
        }
        
        enableEdgeToEdge()
        setContent {
            CSCI3310ProjectTheme {
                MainScreen(this)
            }
        }
    }
}

@Composable
fun MainScreen(activity: MainActivity) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AlarmTest(activity, innerPadding)
    }
}