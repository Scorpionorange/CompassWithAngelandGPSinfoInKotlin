package com.greedisland.compasswithangelandgpsinfoinkotlin

import android.animation.ObjectAnimator
import android.content.Context
import android.hardware.*
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), Runnable{

    private var currentDirection: Int = 0
    private var mTargetDirection: Int = 0
    private var accelerometerValues = FloatArray(3)
    private var magneticFieldValues = FloatArray(3)
    private lateinit var mSensorManager: SensorManager
    private lateinit var mOrientationSensor: Sensor
    private lateinit var mMagneticSensor: Sensor
    private lateinit var mOrientationListener: MySensorEventListener
    private lateinit var mMagneticListener: MySensorEventListener

    override fun run() {
        calculateOrientation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initServices()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onResume(){
        super.onResume()
        mOrientationListener = MySensorEventListener()
        mMagneticListener = MySensorEventListener()
        mSensorManager.registerListener(mOrientationListener, mOrientationSensor, Sensor.TYPE_ACCELEROMETER)
        mSensorManager.registerListener(mMagneticListener, mMagneticSensor, Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onPause() {
        //取消注册
        mSensorManager.unregisterListener(mOrientationListener)
        mSensorManager.unregisterListener(mMagneticListener)
        super.onPause()
    }

    override fun onStop() {
        //取消注册
        mSensorManager.unregisterListener(mOrientationListener)
        mSensorManager.unregisterListener(mMagneticListener)
        super.onStop()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    inner class MySensorEventListener : SensorEventListener2 {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values
            }
            calculateOrientation()
            imageRotation()
            currentDirection = mTargetDirection

            when(currentDirection){
                0 -> angle.text = "正北 / " + 0 + "°"
                90 -> angle.text = "正西 / " + 270 + "°"
                -90 -> angle.text = "正东 / " + 90 + "°"
                in 1..89 -> angle.text = "西北 / " + (360 - currentDirection).toString() + "°"
                in -89..-1 -> angle.text = "东北 / " + Math.abs(currentDirection).toString() + "°"
                in 91..179 -> angle.text = "西南 / " + (360 - currentDirection).toString() + "°"
                in -179..-91 -> angle.text = "东南 / " + Math.abs(currentDirection).toString() + "°"
                else -> angle.text = "正南 / " + 180 + "°"
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onFlushCompleted(sensor: Sensor?) {}

    }

    fun initServices() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) //加速度传感器
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)   //地磁场传感器
    }

    fun calculateOrientation() {
        val values = FloatArray(3)
        val RValues = FloatArray(9)
        // 更新旋转矩阵.
        // 参数1：
        // 参数2：将磁场数据转换进实际的重力坐标中,一般默认情况下可以设置为null
        // 参数3：加速度
        // 参数4：地磁
        SensorManager.getRotationMatrix(RValues, null, accelerometerValues, magneticFieldValues)
        //根据旋转矩阵计算设备的方向
        //参数1：旋转矩阵
        //参数2：模拟方向传感器的数据
        SensorManager.getOrientation(RValues, values)
        values[0] = Math.toDegrees(values[0].toDouble()).toFloat()

        //mTargetDirection = (-values[0]).roundToLong().toFloat()
        mTargetDirection = -values[0].roundToInt()
    }

    fun imageRotation(){
        //顺时针转动为正，故手机顺时针转动时，图片得逆时针转动
        //让图片相对自身中心点转动，开始角度默认为0；此后开始角度等于上一次结束角度
        val animator = ObjectAnimator.ofFloat(
            compassImage, "rotation", currentDirection.toFloat(), mTargetDirection.toFloat())
        animator.duration = 100
        animator.start()
    }
}