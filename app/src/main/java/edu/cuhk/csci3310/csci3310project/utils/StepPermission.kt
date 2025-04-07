package edu.cuhk.csci3310.csci3310project.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat

object StepPermission {
    // 权限状态
    private var isActivityRecognitionPermissionGranted = mutableStateOf(false)
    private var showPermissionDialog = mutableStateOf(false)

    // 权限请求启动器
    private lateinit var activityRecognitionPermissionLauncher: ActivityResultLauncher<String>

    // 初始化权限请求启动器
    fun initializePermissionLaunchers(activity: ComponentActivity) {
        activityRecognitionPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            isActivityRecognitionPermissionGranted.value = isGranted
            if (isGranted) {
                Log.d("StepPermission", "活动识别权限已授予")
            } else {
                Log.w("StepPermission", "活动识别权限被拒绝")
                showPermissionDialog.value = true
            }
        }
    }

    // 检查并请求权限
    fun checkAndRequestPermissions(activity: ComponentActivity): Boolean {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACTIVITY_RECOGNITION)
            != PackageManager.PERMISSION_GRANTED) {
            activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            return false
        } else {
            isActivityRecognitionPermissionGranted.value = true
            return true
        }
    }

    // 打开应用设置
    fun openAppSettings(activity: ComponentActivity) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }

    // 获取权限状态
    fun isActivityRecognitionPermissionGranted(): Boolean {
        return isActivityRecognitionPermissionGranted.value
    }

    fun shouldShowPermissionDialog(): Boolean {
        return showPermissionDialog.value
    }

    fun setShowPermissionDialog(show: Boolean) {
        showPermissionDialog.value = show
    }

    fun requestActivityRecognitionPermission(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activityRecognitionPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }
} 