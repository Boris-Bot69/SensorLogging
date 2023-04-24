package com.example.sensorlogger

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.sensorlogger.model.Acceleration
import com.example.sensorlogger.model.Gravity
import com.example.sensorlogger.model.Gyroscope
import com.example.sensorlogger.model.Summary
import com.example.sensorlogger.viewmodel.SummaryViewModel

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity(), SensorEventListener, LocationListener {

    private lateinit var summaryViewModel: SummaryViewModel

    private lateinit var tvlatitude: TextView

    private lateinit var tvlongitude: TextView
    private lateinit var manager: LocationManager

    //Textview of each X,Y,Z Sensor Data
    private var tvGravity: ArrayList<TextView> = ArrayList()
    private var tvAcceleration: ArrayList<TextView> = ArrayList()
    private var tvGyro: ArrayList<TextView> = ArrayList()

    //ID of Sensor
    private var idGravity: ArrayList<Int> =
        arrayListOf(R.id.Gravity_x, R.id.Gravity_y, R.id.Gravity_z)
    private var idAcceleration: ArrayList<Int> =
        arrayListOf(R.id.Acceleration_x, R.id.Acceleration_y, R.id.Acceleration_z)
    private var idGyro: ArrayList<Int> =
        arrayListOf(R.id.Gyroscope_x, R.id.Gyroscope_y, R.id.Gyroscope_z)

    //Button
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var btnReset: Button
    private lateinit var btnDownload: Button
    private lateinit var btnExport: Button

    //Switch
    private lateinit var switchgrav: Switch
    private lateinit var switchacce: Switch
    private lateinit var switchgyro: Switch
    private lateinit var switchlocation: Switch

    //Sensor
    private lateinit var sensorManager: SensorManager
    private var sensorGravity: Sensor? = null
    private var sensorAcceleration: Sensor? = null
    private var sensorGyro: Sensor? = null

    //SensorData
    private var gravityData: SensorData? = null
    private var accelerationData: SensorData? = null
    private var gyroData: SensorData? = null

    //GyroDaten:
    private var gyroX: Float = 0f
    private var gyroY: Float = 0f
    private var gyroZ: Float = 0f

    //timedifference between two recieved Sensordata in Milliseconds
    private var dt: Long = 1000
    private var timeGravity: Long = 0
    private var timeAcceleration: Long = 0
    private var timeGyro: Long = 0


    private var isAccelData: Boolean = false
    private var isGyroData: Boolean = false
    private var isGravData: Boolean = false

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        summaryViewModel = ViewModelProvider(this)[SummaryViewModel::class.java]

        initView()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.del_database -> {
             summaryViewModel.deleteGravity()
             summaryViewModel.deleteAcceleration()
             summaryViewModel.deleteGyroscope()
             summaryViewModel.deleteLocation()
             summaryViewModel.deleteSummary()
             Toast.makeText(this, "Database cleared!", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> {
                return false
            }
        }
    }



    @SuppressLint("ResourceType")
    private fun initView() {
        for (i in idGravity) {
            tvGravity.add(findViewById(i))
        }

        for (i in idAcceleration) {
            tvAcceleration.add(findViewById(i))
        }
        for (i in idGyro) {
            tvGyro.add(findViewById(i))
        }

        tvlatitude = findViewById(R.id.latitude)
        tvlongitude = findViewById(R.id.longitude)

        btnStart = findViewById(R.id.btn_start)
        btnStop = findViewById(R.id.btn_stop)
        btnReset = findViewById(R.id.btnReset)
        btnExport = findViewById(R.id.export)
        btnDownload = findViewById(R.id.download)

        switchgrav = findViewById(R.id.gravity_switch)
        switchacce = findViewById(R.id.acceleration_switch)
        switchgyro = findViewById(R.id.gyro_switch)
        switchlocation = findViewById(R.id.location_switch)

        btnStart.isEnabled = true
        btnStop.isEnabled = false
        btnReset.isEnabled = false

        //Start Button


        btnStart.setOnClickListener {

            if (switchgrav.isChecked || switchacce.isChecked || switchgyro.isChecked || switchlocation.isChecked) {
                registerListener() // register Sensor
                if (switchlocation.isChecked) {
                    getLocation()  // register Location
                }
                btnStart.isEnabled = false
                btnStop.isEnabled = true
                btnReset.isEnabled = false
            } else {
                Toast.makeText(
                    this,
                    "You have to turn at least one Sensor on to record!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        //Stop Button
        btnStop.setOnClickListener {
            Log.d("Sensors", "Stop button Pressed")
            unregisterListener()  // deregister Sensor
            if (switchlocation.isChecked) {
                manager.removeUpdates(this)  //stop update Location

            }
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            btnReset.isEnabled = true

            switchgrav.isActivated = false
            switchacce.isActivated = false
            switchgyro.isActivated = false



        }
        // Reset Button
        btnReset.setOnClickListener {
            if (switchgrav.isChecked) {

                tvGravity[0].text = findViewById(R.string.first_coordinate)
                tvGravity[1].text = findViewById(R.string.second_coordinate)
                tvGravity[2].text = findViewById(R.string.third_coordinate)
            }

            if (switchacce.isChecked) {
                tvAcceleration[0].text = findViewById(R.string.first_coordinate)
                tvAcceleration[1].text = findViewById(R.string.second_coordinate)
                tvAcceleration[2].text = findViewById(R.string.third_coordinate)
            }
            if (switchgyro.isChecked) {
                tvGyro[0].text = findViewById(R.string.first_coordinate)
                tvGyro[1].text = findViewById(R.string.second_coordinate)
                tvGyro[2].text = findViewById(R.string.third_coordinate)
            }

            if (switchlocation.isChecked) {
                tvlatitude.text = "latitude:"
                tvlongitude.text = "longitude:"
            }
        }
        btnExport.isEnabled = false
        btnDownload.isEnabled = false

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




    //----------------------------------------- Location-----------------------------------------------------
    private fun getLocation() {
        manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                123
            )
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

    }

    override fun onLocationChanged(location: Location) {
        tvlatitude.text = "latitude: " + "${"%.4f".format(location.latitude)}°"
        tvlongitude.text = "longitude: " + "${"%.4f".format(location.longitude)}°"


        val location = Summary(0,DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now()), null, null, null,
            com.example.sensorlogger.model.Location(0,"${"%.6f".format(location.latitude)}°",
            "${"%.6f".format(location.longitude)}°"))
        summaryViewModel.addLocation(location)

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    //----------------------------------------- Location-----------------------------------------------------

    //----------------------------------------- Sensor ------------------------------------------------------
    private fun registerListener() {

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null && switchgrav.isChecked) {
            Log.d(TAG, "registerGravity: on")
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_FASTEST)
            Log.d(TAG, "registerGravity: successful")
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null && switchacce.isChecked) {
            Log.d(TAG, "registerAcceleration: on")
            sensorManager.registerListener(
                this,
                sensorAcceleration,
                SensorManager.SENSOR_DELAY_FASTEST
            )
            Log.d(TAG, "registerAcceleration: successful")
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null && switchgyro.isChecked) {
            Log.d(TAG, "registerGyroscope: on")
            sensorManager.registerListener(this, sensorGyro, SensorManager.SENSOR_DELAY_FASTEST)
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
            isGravData = true
        }
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            isAccelData = true
        }
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            isGyroData = true

        }

        if ((isAccelData) && (isGyroData) && (isGravData)) {
            Log.d(
                "Gravity",
                "Grav_X:" + event.values[0] + "Grav_Y:" + event.values[1] + "Grav_Z:" + event.values[2]
            )
            getGravityData(event)

            Log.d(
                "Accelerometer",
                "Acc_X:" + event.values[0] + "Acc_Y:" + event.values[1] + "Acc_Z:" + event.values[2]
            )
            getAccelerationData(event)
            Log.d(
                "Gyroscope",
                "Gyro_X:" + event.values[0] + "Gyro_Y:" + event.values[1] + "Gyro_Z:" + event.values[2]
            )
            getGyroData(event)

            isGyroData = false
            isAccelData = false
            isGravData = false
        }


    }

    override fun onAccuracyChanged(event: Sensor?, p1: Int) {
    }

    private fun getGravityData(f: SensorEvent?) {


        if (gravityData == null) {
            gravityData = SensorData(f!!.values[0], f!!.values[1], f!!.values[2], f!!.timestamp)
        } else {
            gravityData!!.x1 = f!!.values[0]
            gravityData!!.x2 = f!!.values[1]
            gravityData!!.x3 = f!!.values[2]

        }

        if (System.currentTimeMillis() - timeGravity >= dt) {
            tvGravity[0].text = "X: ${"%.2f".format(gravityData!!.x1)} m/s^2"
            tvGravity[1].text = "Y: ${"%.2f".format(gravityData!!.x2)} m/s^2"
            tvGravity[2].text = "Z: ${"%.2f".format(gravityData!!.x3)} m/s^2"
            timeGravity = System.currentTimeMillis()

            val gravity = Summary(0, DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()), Gravity(
                    0,"${"%.2f".format(gravityData!!.x1)} m/s^2" , "${
                "%.2f".format(
                    gravityData!!.x2
                )
            } m/s^2", "${"%.2f".format(gravityData!!.x2)} m/s^2"), null, null, null)
            summaryViewModel.addGravity(gravity)
        }
    }

    private fun getAccelerationData(e: SensorEvent?) {
        if (accelerationData == null) {
            accelerationData =
                SensorData(e!!.values[0], e!!.values[1], e!!.values[2], e!!.timestamp)
        } else {
            accelerationData!!.x1 = e!!.values[0]
            accelerationData!!.x2 = e!!.values[1]
            accelerationData!!.x3 = e!!.values[2]

        }
        if (System.currentTimeMillis() - timeAcceleration >= dt) {
            tvAcceleration[0].text = "X: ${"%.2f".format(accelerationData!!.x1)} m/s^2"
            tvAcceleration[1].text = "Y: ${"%.2f".format(accelerationData!!.x2)} m/s^2"
            tvAcceleration[2].text = "Z: ${"%.2f".format(accelerationData!!.x3)} m/s^2"
            timeAcceleration = System.currentTimeMillis()

            val acceleration = Summary(0,DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneOffset.UTC)
                .format(Instant.now()),null,
                Acceleration(
                0,"${"%.2f".format(accelerationData!!.x1)} m/s^2" , "${
                    "%.2f".format(
                        accelerationData!!.x2
                    )
                } m/s^2", "${"%.2f".format(accelerationData!!.x2)} m/s^2"), null, null)
            summaryViewModel.addAcceleration(acceleration)
        }

    }

    private fun getGyroData(e: SensorEvent?) {
        if (gyroData == null) {
            gyroData = SensorData(e!!.values[0], e!!.values[1], e!!.values[2], e!!.timestamp)
        } else {
            gyroData!!.x1 = e!!.values[0]
            gyroData!!.x2 = e!!.values[1]
            gyroData!!.x3 = e!!.values[2]


            if (System.currentTimeMillis() - timeGyro >= dt) {

                tvGyro[0].text =
                    "X: ${"%.2f".format(gyroData!!.x1)}°/s"
                tvGyro[1].text =
                    "Y: ${"%.2f".format(gyroData!!.x2)} °/s"
                tvGyro[2].text =
                    "Z: ${"%.2f".format(gyroData!!.x3)} °/s"

                timeGyro = System.currentTimeMillis()
                val gyroscope = Summary(0,DateTimeFormatter
                    .ofPattern("yyyy-MM-dd HH:mm:ss")
                    .withZone(ZoneOffset.UTC)
                    .format(Instant.now()),null,null,
                    Gyroscope(
                        0,"${"%.2f".format(gyroData!!.x1)} °/s",
                        "${"%.2f".format(gyroData!!.x2)} °/s",
                        "${"%.2f".format(gyroData!!.x3)} °/s"), null)

                summaryViewModel.addGyroscope(gyroscope)

            }

        }
    }

}