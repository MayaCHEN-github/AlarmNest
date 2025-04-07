package edu.cuhk.csci3310.csci3310project

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.cuhk.csci3310.csci3310project.frontend.screens.AlarmOffScreen
import edu.cuhk.csci3310.csci3310project.frontend.screens.AlarmScreen
import edu.cuhk.csci3310.csci3310project.frontend.screens.AlarmTypingScreen
import edu.cuhk.csci3310.csci3310project.frontend.screens.ClockListScreen
import edu.cuhk.csci3310.csci3310project.frontend.viewmodels.ClockListScreenViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cuhk.csci3310.csci3310project.backend.data.Alarm
import edu.cuhk.csci3310.csci3310project.backend.data.AlarmDatabaseFacade
import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AppNavigation(
    navController: NavHostController,
    activity: MainActivity,
    onStopAlarm: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "clock_list_screen"
    ) {
        composable("clock_list_screen") {
            val viewModel: ClockListScreenViewModel = viewModel(
                factory = ClockListScreenViewModel.Factory(activity)
            )
            ClockListScreen(viewModel = viewModel)
        }
        
        composable("alarm_screen/{alarmId}/{isSubAlarm}/{alarmLabel}") { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")?.toLongOrNull() ?: -1L
            val isSubAlarm = backStackEntry.arguments?.getString("isSubAlarm")?.toBoolean() ?: false
            val alarmLabel = backStackEntry.arguments?.getString("alarmLabel") ?: "闹钟提醒"
            
            var alarm by remember { mutableStateOf<Alarm?>(null) }
            
            LaunchedEffect(Unit) {
                if (alarmId != -1L) {
                    val dbAlarm = if (isSubAlarm) {
                        // 如果是子闹钟，获取父闹钟
                        val subAlarm = AlarmDatabaseFacade.getSubAlarmById(activity, alarmId)
                        if (subAlarm != null) {
                            AlarmDatabaseFacade.getAlarmById(activity, subAlarm.parentAlarmId)
                        } else null
                    } else {
                        AlarmDatabaseFacade.getAlarmById(activity, alarmId)
                    }
                    
                    if (dbAlarm != null) {
                        alarm = dbAlarm
                        Log.d("AppNavigation", "从数据库获取到闹钟: $dbAlarm")
                    } else {
                        Log.e("AppNavigation", "未找到闹钟: alarmId=$alarmId, isSubAlarm=$isSubAlarm")
                    }
                } else {
                    Log.e("AppNavigation", "无效的闹钟ID: $alarmId")
                }
            }
            
            AlarmScreen(
                navController = navController,
                alarm = alarm,
                onStartTask = {
                    navController.navigate("typing_alarm_screen") {
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
                onStopAlarm = onStopAlarm
            )
        }
    }
} 