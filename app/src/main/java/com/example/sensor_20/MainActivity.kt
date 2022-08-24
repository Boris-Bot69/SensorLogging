package com.example.sensor_20


import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.format.DateTimeFormatter



private val TAG = MainActivity::class.simpleName


class MainActivity : AppCompatActivity(), SensorEventListener{
    //--------------------------Sensor-----------------------------------------------------------------------------
    //View
    private var tvGravity: ArrayList<TextView> = ArrayList()
    private var tvAcceleration: ArrayList<TextView> = ArrayList()
    private var tvGyro: ArrayList<TextView> = ArrayList()
    //ID
    private var idGravity: ArrayList<Int> = arrayListOf(R.id.Gravity_x, R.id.Gravity_y, R.id.Gravity_z)
    private var idAcceleration: ArrayList<Int> = arrayListOf(R.id.Acceleration_x,R.id.Acceleration_y,R.id.Acceleration_z)
    private var idGyro: ArrayList<Int> = arrayListOf(R.id.Gyroscope_x, R.id.Gyroscope_y, R.id.Gyroscope_z)

    //Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button

    //Sensor

    private lateinit var sensorManager: SensorManager
    private var sensorGravity: Sensor? = null
    private var sensorAcceleration: Sensor? = null
    private var sensorGyro: Sensor? = null

    //SensorData
    private var gravityData: SensorData? = null
    private var accelerationData: SensorData? = null
    private var gyroData:SensorData? = null

    //GyroDaten:

    private var gyroX: Float = 0f
    private var gyroY: Float = 0f
    private var gyroZ: Float = 0f

    //time
    private var dt:Long = 1000

    private var timeGravity: Long = 0
    private var timeAcceleration: Long = 0
    private var timeGyro: Long = 0

    //View IDs


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSensors()

    }
  


    private fun initSensors(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        }
    }
    private fun initViews() {
        
        // initiate sensor
        for (i in idGravity) {
            tvGravity.add(findViewById(i))
        }

        for (i in idAcceleration) {
            tvAcceleration.add(findViewById(i))
        }
        for (i in idGyro) {
            tvGyro.add(findViewById(i))
        }
        
        //initiate location


        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)


        btnStart.setOnClickListener {
            registerListener() // Sensor
            btnStart.isEnabled = false
            btnStop.isEnabled = true
        }
        btnStop.setOnClickListener {
            Log.d("Sensors", "Stop button Pressed")
            unregisterListener()
            btnStart.isEnabled = true
            btnStop.isEnabled = false
        }
    }


    private fun registerListener() {

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            Log.d(TAG, "registerGravity: on")
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "registerGravity: successful")

        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            Log.d(TAG, "registerAcceleration: on")
            sensorManager.registerListener(this, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "registerAcceleration: successful")

        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            Log.d(TAG, "registerGyroscope: on")
            sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "registerGyroscope: successful")

        }

    }
    private fun unregisterListener() {
        sensorManager.unregisterListener(this, sensorGravity)
        sensorManager.unregisterListener(this, sensorAcceleration)
        sensorManager.unregisterListener(this, sensorGyro)

    }
    override fun onSensorChanged(event: SensorEvent?) {

        if (event!!.sensor.type == Sensor.TYPE_GRAVITY) {
                Log.d("Gravity", "Grav_X:" + event.values[0] + "Grav_Y:" + event.values[1] + "Grav_Z:" + event.values[2])

            getGravityData(event)
        }
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
                Log.d("Accelerometer", "Acc_X:" + event.values[0] + "Acc_Y:" + event.values[1] + "Acc_Z:" + event.values[2])

            getAccelerationData(event)
        }
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                Log.d("Gyroscope", "Gyro_X:" + event.values[0] + "Gyro_Y:" + event.values[1] + "Gyro_Z:" + event.values[2])
                getGyroData(event)
        }


    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {
    }

    private fun getGravityData(f: SensorEvent?) {

        if (gravityData == null) {
            gravityData = SensorData(f!!.values[0], f!!.values[1], f!!.values[2], f!!.timestamp)
        }
        else {
            gravityData!!.x1 = f!!.values[0]
            gravityData!!.x2 = f!!.values[1]
            gravityData!!.x3 = f!!.values[2]

        }

        if (System.currentTimeMillis() - timeGravity >= dt) {
            tvGravity[0].text = "x1: ${"%.2f".format(gravityData!!.x1)} m/s^2"
            tvGravity[1].text = "x2: ${"%.2f".format(gravityData!!.x2)} m/s^2"
            tvGravity[2].text = "x3: ${"%.2f".format(gravityData!!.x3)} m/s^2"
            timeGravity = System.currentTimeMillis()

            saveGravity(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) +
                    "   Gravity_x: ${"%.2f".format(gravityData!!.x1)} m/s^2" + " Gravity_y: ${"%.2f".format(gravityData!!.x2)} m/s^2"+ " Gravity_z: ${"%.2f".format(gravityData!!.x2)} m/s^2 \n")
        }
    }

    private fun saveGravity(s: String) {
        try {
            // You can find the file in /data/user/0/com.example.sensor_20/files/Logging.csv
            openFileOutput("Logging_Gravity.csv" , Context.MODE_APPEND).use {
                    fos -> OutputStreamWriter(fos).use {
                    osw -> osw.write(s)
                    }
            }

        } catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${filesDir.absolutePath}")

        }

    }


    private fun getAccelerationData(e: SensorEvent?) {
        if (accelerationData == null) {
            accelerationData = SensorData(e!!.values[0], e!!.values[1], e!!.values[2], e!!.timestamp)
        }
        else {
            accelerationData!!.x1 = e!!.values[0]
            accelerationData!!.x2 = e!!.values[1]
            accelerationData!!.x3 = e!!.values[2]

        }
        if (System.currentTimeMillis() - timeAcceleration >= dt) {
            tvAcceleration[0].text = "x1: ${"%.2f".format(accelerationData!!.x1)} m/s^2"
            tvAcceleration[1].text = "x2: ${"%.2f".format(accelerationData!!.x2)} m/s^2"
            tvAcceleration[2].text = "x3: ${"%.2f".format(accelerationData!!.x3)} m/s^2"
            timeAcceleration = System.currentTimeMillis()


            saveAcceleration(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) +
                    "   Acc_x: ${"%.2f".format(accelerationData!!.x1)} m/s^2" + " Acc_y: ${"%.2f".format(accelerationData!!.x2)} m/s^2"+ " Acc_z: ${"%.2f".format(accelerationData!!.x2)} m/s^2 \n")

        }

    }

    private fun saveAcceleration(s: String) {
        try {
            // You can find the file in /data/user/0/com.example.sensor_20/files/Logging.csv
            openFileOutput("Logging_Acceleration.csv" , Context.MODE_APPEND).use {
                    fos -> OutputStreamWriter(fos).use {
                    osw -> osw.write(s)
            }
            }

        } catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${filesDir.absolutePath}")

        }

    }

    private fun getGyroData(e: SensorEvent?) {
        if(gyroData == null) {
            gyroData = SensorData(e!!.values[0],e!!.values[1], e!!.values[2], e!!.timestamp )
            timeGyro = System.currentTimeMillis()
        }
        else {
            var time = (System.currentTimeMillis() - timeGyro) /1000f // Time-Difference in s
            gyroData!!.x1 = e!!.values[0]
            gyroData!!.x2 = e!!.values[1]
            gyroData!!.x3 = e!!.values[2]
            gyroX += gyroData!!.x1 * time
            gyroX += gyroData!!.x2 * time
            gyroX += gyroData!!.x3 * time

            if (System.currentTimeMillis() - timeGyro >= dt) {

                tvGyro[0].text = "x1: ${"%.2f".format(gyroData!!.x1*(180.0/Math.PI))} °/s \t\t gyroX: ${"%.2f".format(gyroX*(180.0/Math.PI))} °"
                tvGyro[1].text = "x2: ${"%.2f".format(gyroData!!.x2*(180.0/Math.PI))} °/s \t\t gyroY: ${"%.2f".format(gyroY*(180.0/Math.PI))} °"
                tvGyro[2].text = "x3: ${"%.2f".format(gyroData!!.x3*(180.0/Math.PI))} °/s \t\t gyroZ: ${"%.2f".format(gyroZ*(180.0/Math.PI))} °"
                timeGyro = System.currentTimeMillis()

                saveGyro(DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                        +
                    "   x1: ${"%.2f".format(gyroData!!.x1 * (180.0 / Math.PI))} °/s \t\t gyroX: ${"%.2f".format(gyroX * (180.0 / Math.PI))} °" +
                        "x2: ${"%.2f".format(gyroData!!.x1 * (180.0 / Math.PI))} °/s \t\t gyroY: ${"%.2f".format(gyroY * (180.0 / Math.PI))} °" +
                        "x3: ${"%.2f".format(gyroData!!.x1 * (180.0 / Math.PI))} °/s \t\t gyroZ: ${"%.2f".format(gyroX * (180.0 / Math.PI))} °\n")

            }

        }
    }

    private fun saveGyro(s: String) {
        try {
            // You can find the file in /data/user/0/com.example.sensor_20/files/Logging.csv
            openFileOutput("Logging_Gyro.csv" , Context.MODE_APPEND).use {
                    fos -> OutputStreamWriter(fos).use {
                    osw -> osw.write(s)
            }
            }

        } catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${filesDir.absolutePath}")

        }

    }


}
