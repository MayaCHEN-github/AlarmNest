package edu.cuhk.csci3310.csci3310project.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class StepSensor(private val context: Context) : SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var stepDetector: Sensor? = null
    private var stepCount = 0
    private var targetSteps = 20
    private var onStepListener: OnStepListener? = null

    interface OnStepListener {
        fun onStepChanged(remainingSteps: Int)
        fun onTargetReached()
    }

    fun setOnStepListener(listener: OnStepListener) {
        this.onStepListener = listener
    }

    fun setTargetSteps(steps: Int) {
        targetSteps = steps
        stepCount = 0
        onStepListener?.onStepChanged(targetSteps - stepCount)
    }

    fun start() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        
        if (stepDetector == null) {
            Log.w("StepSensor", "Step detector sensor not available")
            return
        }
        
        val success = sensorManager?.registerListener(this, stepDetector, SensorManager.SENSOR_DELAY_NORMAL)
        if (success == true) {
            Log.d("StepSensor", "Successfully registered step detector listener")
        } else {
            Log.e("StepSensor", "Failed to register step detector listener")
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++
            val remainingSteps = targetSteps - stepCount
            Log.d("StepSensor", "Step detected. Remaining steps: $remainingSteps")
            onStepListener?.onStepChanged(remainingSteps)
            
            if (remainingSteps <= 0) {
                onStepListener?.onTargetReached()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
}

