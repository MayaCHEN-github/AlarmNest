package edu.cuhk.csci3310.csci3310project.utils

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
import androidx.compose.runtime.mutableStateOf

/* AlarmPermission.kt
 * 用于处理闹钟相关的权限请求和状态管理，包括存储权限和通知权限的请求。以及检查精确闹钟权限。
* */

object AlarmPermission {
    private const val TAG = "AlarmPermission"

    // 权限状态
    private var isStoragePermissionGranted = mutableStateOf(false)
    private var isNotificationPermissionGranted = mutableStateOf(false)
    private var showStoragePermissionDialog = mutableStateOf(false)

    // 权限请求启动器
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var storagePermissionLauncher: ActivityResultLauncher<Array<String>>

    // 初始化权限请求启动器
    fun initializePermissionLaunchers(activity: ComponentActivity) {
        Log.d(TAG, "初始化权限请求启动器")
        notificationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            isNotificationPermissionGranted.value = isGranted
            if (isGranted) {
                Log.i(TAG, "通知权限已授予")
            } else {
                Log.w(TAG, "通知权限被拒绝")
            }
        }

        storagePermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                Log.i(TAG, "基本存储权限已授予")
                requestManageExternalStoragePermission(activity)
            } else {
                Log.w(TAG, "基本存储权限被拒绝")
                showStoragePermissionDialog.value = true
            }
        }
    }

    // 检查并请求所有必要权限
    fun checkAndRequestPermissions(activity: ComponentActivity): Boolean {
        Log.d(TAG, "开始检查并请求所有必要权限")
        // 检查精确闹钟权限
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.w(TAG, "精确闹钟权限未授予，跳转到设置页面")
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivity(intent)
            return false
        }

        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "请求通知权限")
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return false
            } else {
                Log.d(TAG, "通知权限已授予")
                isNotificationPermissionGranted.value = true
            }
        }

        // 检查存储权限
        val hasStoragePermission = Environment.isExternalStorageManager()
        Log.d(TAG, "检查存储权限状态: $hasStoragePermission")
        isStoragePermissionGranted.value = hasStoragePermission
        
        if (!hasStoragePermission) {
            Log.d(TAG, "请求存储权限")
            requestStoragePermissions(activity)
            return false
        }

        Log.i(TAG, "所有必要权限已授予")
        return true
    }

    // 检查基本存储权限
    private fun hasBasicStoragePermissions(activity: ComponentActivity): Boolean {
        return ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // 请求存储权限
    private fun requestStoragePermissions(activity: ComponentActivity) {
        Log.d(TAG, "开始请求存储权限")
        if (Environment.isExternalStorageManager()) {
            Log.d(TAG, "已获得管理外部存储权限")
            isStoragePermissionGranted.value = true
        } else {
            Log.d(TAG, "请求管理外部存储权限")
            requestManageExternalStoragePermission(activity)
        }
    }

    // 请求管理外部存储权限
    private fun requestManageExternalStoragePermission(activity: ComponentActivity) {
        Log.d(TAG, "跳转到管理外部存储权限设置页面")
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        try {
            activity.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "跳转设置页面失败: ${e.message}")
            val alternativeIntent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            activity.startActivity(alternativeIntent)
        }
    }

    // 打开应用设置
    fun openAppSettings(activity: ComponentActivity) {
        Log.d(TAG, "打开应用设置页面")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }
        activity.startActivity(intent)
    }

    // 获取存储权限状态
    fun isStoragePermissionGranted(): Boolean {
        val hasPermission = Environment.isExternalStorageManager()
        Log.d(TAG, "检查存储权限状态: $hasPermission")
        isStoragePermissionGranted.value = hasPermission
        return hasPermission
    }

    // 获取通知权限状态
    fun isNotificationPermissionGranted(): Boolean {
        return isNotificationPermissionGranted.value
    }

    // 获取是否显示存储权限对话框
    fun shouldShowStoragePermissionDialog(): Boolean {
        return showStoragePermissionDialog.value
    }

    // 设置是否显示存储权限对话框
    fun setShowStoragePermissionDialog(show: Boolean) {
        showStoragePermissionDialog.value = show
    }

    fun requestNotificationPermission(activity: ComponentActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

