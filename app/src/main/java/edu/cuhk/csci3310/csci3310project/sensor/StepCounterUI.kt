package edu.cuhk.csci3310.csci3310project.sensor

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class StepCounterViewModel {
    private var stepSensor: StepSensor? = null
    private var _remainingSteps = mutableStateOf(20)
    private var _isTargetReached = mutableStateOf(false)
    
    val remainingSteps: State<Int> = _remainingSteps
    val isTargetReached: State<Boolean> = _isTargetReached
    
    fun initialize(context: Context) {
        stepSensor = StepSensor(context)
        stepSensor?.setTargetSteps(20)
        stepSensor?.setOnStepListener(object : StepSensor.OnStepListener {
            override fun onStepChanged(remainingSteps: Int) {
                _remainingSteps.value = remainingSteps
            }

            override fun onTargetReached() {
                _isTargetReached.value = true
            }
        })
    }
    
    fun start() {
        stepSensor?.start()
    }
    
    fun stop() {
        stepSensor?.stop()
    }
}

@Composable
fun StepCounterUI(viewModel: StepCounterViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 计步器UI
        Text(
            text = if (viewModel.isTargetReached.value) "Well done!" else "${viewModel.remainingSteps.value}",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // 提示信息
        Text(
            text = if (viewModel.isTargetReached.value) 
                "恭喜你完成了目标步数！" 
            else 
                "请开始行走，目标步数：20步",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
} 