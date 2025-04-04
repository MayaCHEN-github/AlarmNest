package edu.cuhk.csci3310.csci3310project.alarm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

object AlarmPermission {
    // 权限状态
    private var isStoragePermissionGranted = mutableStateOf(false)
    private var isNotificationPermissionGranted = mutableStateOf(false)
    private var showStoragePermissionDialog = mutableStateOf(false)

    // 权限请求启动器
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Array<String>>

    // 初始化权限请求启动器
    fun initializePermissionLaunchers(activity: ComponentActivity) {
        notificationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            isNotificationPermissionGranted.value = isGranted
            if (isGranted) {
                Log.d("AlarmPermission", "通知权限已授予")
            } else {
                Log.w("AlarmPermission", "通知权限被拒绝")
            }
        }

        storagePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Log.d("AlarmPermission", "基本存储权限已授予")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestManageExternalStoragePermission(activity)
                } else {
                    isStoragePermissionGranted.value = true
                }
            } else {
                Log.w("AlarmPermission", "基本存储权限被拒绝")
                showStoragePermissionDialog.value = true
            }
        }
    }

    // 检查并请求所有必要权限
    fun checkAndRequestPermissions(activity: ComponentActivity): Boolean {
        // 检查精确闹钟权限
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivity(intent)
            return false
        }

        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return false
            } else {
                isNotificationPermissionGranted.value = true
            }
        }

        // 检查存储权限
        if (!isStoragePermissionGranted()) {
            requestStoragePermissions(activity)
            return false
        }

        return true
    }

    // 检查基本存储权限
    private fun hasBasicStoragePermissions(activity: ComponentActivity): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // 请求存储权限
    private fun requestStoragePermissions(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                isStoragePermissionGranted.value = true
            } else {
                requestManageExternalStoragePermission(activity)
            }
        } else {
            if (hasBasicStoragePermissions(activity)) {
                isStoragePermissionGranted.value = true
            } else {
                requestBasicStoragePermissions()
            }
        }
    }

    // 请求基本存储权限
    private fun requestBasicStoragePermissions() {
        storagePermissionLauncher.launch(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ))
    }

    // 请求管理外部存储权限
    private fun requestManageExternalStoragePermission(activity: ComponentActivity) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            val alternativeIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            activity.startActivity(alternativeIntent)
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
    fun isStoragePermissionGranted(): Boolean {
        return isStoragePermissionGranted.value
    }

    fun isNotificationPermissionGranted(): Boolean {
        return isNotificationPermissionGranted.value
    }

    fun shouldShowStoragePermissionDialog(): Boolean {
        return showStoragePermissionDialog.value
    }

    fun setShowStoragePermissionDialog(show: Boolean) {
        showStoragePermissionDialog.value = show
    }
}

