package edu.cuhk.csci3310.csci3310project.alarm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

object AlarmPermission {
    fun checkAndRequestPermissions(activity: ComponentActivity, permissionLauncher: ActivityResultLauncher<String>): Boolean {
        // 检查精确闹钟权限
        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            activity.startActivity(intent)
            return false
        }

        // 检查通知权限
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return false
        }

        return true
    }

    fun createPermissionLauncher(activity: ComponentActivity): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("AlarmPermission", "通知权限已授予")
            } else {
                Log.w("AlarmPermission", "通知权限被拒绝")
            }
        }
    }
}

