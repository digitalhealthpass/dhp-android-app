package com.merative.healthpass.common.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.merative.healthpass.extensions.loge
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

class ShakeListener(private val mContext: Context) : SensorEventListener {

    private var mSensorMgr: SensorManager? = null
    private var mLastX = -1.0f
    private var mLastY = -1.0f
    private var mLastZ = -1.0f
    private var mLastTime: Long = 0
    private var mShakeCount = 0
    private var mLastShake: Long = 0
    private var mLastForce: Long = 0
    private val events = PublishSubject.create<Any>()

    fun listenToShakeEvents(): Observable<Any> {
        return events
    }

    fun onResume() {
        mSensorMgr = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (mSensorMgr == null) {
            loge("Sensors not supported")
            events.onError(UnsupportedOperationException("Sensors not supported"))
        }
        var supported = false
        try {
            supported = mSensorMgr!!.registerListener(
                this,
                mSensorMgr!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME
            )
        } catch (e: Exception) {
            loge("Shaking not supported", e)
            events.onError(e)
        }

        if (!supported && mSensorMgr != null)
            mSensorMgr!!.unregisterListener(this)
    }

    fun onPause() {
        if (mSensorMgr != null) {
            mSensorMgr!!.unregisterListener(this)
            mSensorMgr = null
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Empty function body
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER)
            return
        val nowInMilliSecond = System.currentTimeMillis()

        if (nowInMilliSecond - mLastForce > SHAKE_TIMEOUT_MILLISECONDS) {
            mShakeCount = 0
        }

        if (nowInMilliSecond - mLastTime > TIME_THRESHOLD_MILLISECONDS) {
            val diff = nowInMilliSecond - mLastTime
            val speed = Math.abs(
                (event.values[INDEX_X_AXIS]
                        + event.values[INDEX_Y_AXIS]
                        + event.values[INDEX_Z_AXIS]) - mLastX - mLastY
                        - mLastZ
            ) / diff * 10000
            if (speed > FORCE_THRESHOLD) {
                if (++mShakeCount >= SHAKE_COUNT && nowInMilliSecond - mLastShake > SHAKE_DURATION_MILLISECONDS) {
                    mLastShake = nowInMilliSecond
                    mShakeCount = 0
                    events.onNext(Any())
                }
                mLastForce = nowInMilliSecond
            }
            mLastTime = nowInMilliSecond
            mLastX = event.values[INDEX_X_AXIS]
            mLastY = event.values[INDEX_Y_AXIS]
            mLastZ = event.values[INDEX_Z_AXIS]
        }
    }

    companion object {
        private const val FORCE_THRESHOLD = 700
        private const val TIME_THRESHOLD_MILLISECONDS = 100
        private const val SHAKE_TIMEOUT_MILLISECONDS = 5000
        private const val SHAKE_DURATION_MILLISECONDS = 1000
        private const val SHAKE_COUNT = 5
        private const val INDEX_X_AXIS =
            0  // Index of the X value in the array returned by android.hardware.SensorListener.onSensorChanged() (extracted from the now deprecated SensorManager.DATA_X)
        private const val INDEX_Y_AXIS =
            1  // Index of the Y value in the array returned by android.hardware.SensorListener.onSensorChanged() (extracted from the now deprecated SensorManager.DATA_Y)
        private const val INDEX_Z_AXIS =
            2  // Index of the Z value in the array returned by android.hardware.SensorListener.onSensorChanged() (extracted from the now deprecated SensorManager.DATA_Z)
    }


}