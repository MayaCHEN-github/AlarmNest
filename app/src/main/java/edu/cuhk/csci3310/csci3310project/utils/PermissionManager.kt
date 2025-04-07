package edu.cuhk.csci3310.csci3310project.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.util.Log

object PermissionManager {
    // 权限状态
    private var currentPermission = mutableStateOf<PermissionType?>(null)
    private var showPermissionDialog = mutableStateOf(false)
    
    // 权限类型枚举
    enum class PermissionType {
        EXACT_ALARM,
        ACTIVITY_RECOGNITION,
        NOTIFICATION,
        STORAGE
    }

    fun initialize(activity: ComponentActivity) {
        AlarmPermission.initializePermissionLaunchers(activity)
        StepPermission.initializePermissionLaunchers(activity)
        // 重置权限状态
        currentPermission.value = null
        showPermissionDialog.value = false
    }

    fun checkAndRequestPermissions(activity: ComponentActivity) {
        Log.d("PermissionManager", "开始检查并请求权限")
        // 如果当前没有正在请求的权限，从第一个开始
        if (currentPermission.value == null) {
            currentPermission.value = PermissionType.EXACT_ALARM
            requestCurrentPermission(activity)
        }
    }

    private fun requestCurrentPermission(activity: ComponentActivity) {
        when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> {
                if (!isExactAlarmPermissionGranted(activity)) {
                    showPermissionDialog.value = true
                } else {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.ACTIVITY_RECOGNITION -> {
                if (!StepPermission.isActivityRecognitionPermissionGranted()) {
                    showPermissionDialog.value = true
                } else {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.NOTIFICATION -> {
                if (!AlarmPermission.isNotificationPermissionGranted()) {
                    showPermissionDialog.value = true
                } else {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.STORAGE -> {
                if (!AlarmPermission.isStoragePermissionGranted()) {
                    showPermissionDialog.value = true
                } else {
                    currentPermission.value = null
                }
            }
            null -> {}
        }
    }

    private fun moveToNextPermission(activity: ComponentActivity) {
        currentPermission.value = when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> PermissionType.ACTIVITY_RECOGNITION
            PermissionType.ACTIVITY_RECOGNITION -> PermissionType.NOTIFICATION
            PermissionType.NOTIFICATION -> PermissionType.STORAGE
            PermissionType.STORAGE -> null
            null -> null
        }
        if (currentPermission.value != null) {
            requestCurrentPermission(activity)
        }
    }

    fun onPermissionConfirmed(activity: ComponentActivity) {
        showPermissionDialog.value = false
        when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                activity.startActivity(intent)
                // 不立即移动到下一个权限，等待用户从设置返回
            }
            PermissionType.ACTIVITY_RECOGNITION -> {
                StepPermission.checkAndRequestPermissions(activity)
                // 如果权限请求成功，移动到下一个权限
                if (StepPermission.isActivityRecognitionPermissionGranted()) {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.NOTIFICATION -> {
                AlarmPermission.checkAndRequestPermissions(activity)
                // 如果权限请求成功，移动到下一个权限
                if (AlarmPermission.isNotificationPermissionGranted()) {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.STORAGE -> {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${activity.packageName}")
                }
                activity.startActivity(intent)
                // 不立即移动到下一个权限，等待用户从设置返回
            }
            null -> {}
        }
    }

    fun onPermissionCancelled(activity: ComponentActivity) {
        showPermissionDialog.value = false
        moveToNextPermission(activity)
    }

    fun checkPermissionStatus(activity: ComponentActivity) {
        when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> {
                if (isExactAlarmPermissionGranted(activity)) {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.ACTIVITY_RECOGNITION -> {
                if (StepPermission.isActivityRecognitionPermissionGranted()) {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.NOTIFICATION -> {
                if (AlarmPermission.isNotificationPermissionGranted()) {
                    moveToNextPermission(activity)
                }
            }
            PermissionType.STORAGE -> {
                if (AlarmPermission.isStoragePermissionGranted()) {
                    currentPermission.value = null
                }
            }
            null -> {}
        }
    }

    private fun isExactAlarmPermissionGranted(activity: ComponentActivity): Boolean {
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return alarmManager.canScheduleExactAlarms()
    }

    fun shouldShowPermissionDialog(): Boolean {
        return showPermissionDialog.value
    }

    fun getPermissionDialogTitle(): String {
        return when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> "需要精确闹钟权限"
            PermissionType.ACTIVITY_RECOGNITION -> "需要活动识别权限"
            PermissionType.NOTIFICATION -> "需要通知权限"
            PermissionType.STORAGE -> "需要存储权限"
            null -> ""
        }
    }

    fun getPermissionDialogContent(): String {
        return when (currentPermission.value) {
            PermissionType.EXACT_ALARM -> "此应用需要精确闹钟权限来准确设置闹钟。请在设置中授予此权限。"
            PermissionType.ACTIVITY_RECOGNITION -> "此应用需要活动识别权限来记录您的步数。请授予此权限。"
            PermissionType.NOTIFICATION -> "此应用需要通知权限来显示闹钟提醒。请授予此权限。"
            PermissionType.STORAGE -> "此应用需要存储权限来保存闹钟数据。请在设置中授予此权限。"
            null -> ""
        }
    }

    fun isStoragePermissionGranted(): Boolean {
        return AlarmPermission.isStoragePermissionGranted()
    }

    fun isNotificationPermissionGranted(): Boolean {
        return AlarmPermission.isNotificationPermissionGranted()
    }

    fun isActivityRecognitionPermissionGranted(): Boolean {
        return StepPermission.isActivityRecognitionPermissionGranted()
    }
} 