package edu.cuhk.csci3310.csci3310project.backend.features.step

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/* StepSensor.kt
 * 一个计步器类，使用 Android 的传感器 API 来检测步数。
 * TODO:它理论上可以计步，但模拟器怎么计步呢……
* */

class StepSensor(private val context: Context) : SensorEventListener {

    companion object {
        private const val TAG = "StepSensor"
    }
    
    private var sensorManager: SensorManager? = null // 传感器管理器
    private var stepDetector: Sensor? = null // 步数检测传感器
    private var stepCount = 0 // 当前已记录的步数
    private var targetSteps = 20 // 目标步数
    private var onStepListener: OnStepListener? = null

    // 步数变化监听器接口
    interface OnStepListener {
        fun onStepChanged(remainingSteps: Int) // 当步数发生变化时调用
        fun onTargetReached() // 当达到目标步数时调用
    }

    // 设置步数变化监听器
    fun setOnStepListener(listener: OnStepListener) {
        this.onStepListener = listener
    }

    // 设置目标步数
    fun setTargetSteps(steps: Int) {
        targetSteps = steps
        stepCount = 0
        onStepListener?.onStepChanged(targetSteps - stepCount)
    }

    // 开始计步
    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        if (stepDetector == null) {
            Log.w(TAG, "步数检测传感器不可用")
            return
        }
        
        val success = sensorManager?.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
        if (success == true) {
            Log.d(TAG, "成功注册步数检测监听器")
        } else {
            Log.e(TAG, "注册步数检测监听器失败")
        }
    }

    /**
     * 停止计步
     * 取消注册传感器监听器
     */
    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    /**
     * 传感器数据变化回调
     * 当检测到步数时更新计数并通知监听器
     */
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            val remainingSteps = targetSteps - stepCount
            Log.d(TAG, "检测到步数。剩余步数：$remainingSteps")
            onStepListener?.onStepChanged(remainingSteps)

            if (remainingSteps <= 0) {
                onStepListener?.onTargetReached()
            }
        }
    }

    // 传感器精度变化回调
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 好像必须实现，不然会报错……所以就空着了？
    }
}

