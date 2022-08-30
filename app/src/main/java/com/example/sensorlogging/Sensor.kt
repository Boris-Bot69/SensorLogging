package com.example.sensorlogging
import android.Manifest
import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorManager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater

import android.content.Context
import android.content.pm.PackageManager


import android.hardware.SensorEvent
import android.hardware.SensorEventListener

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import android.widget.Switch
import android.widget.TextView

import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.format.DateTimeFormatter

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

private val TAG = MainActivity::class.simpleName
private const val FILENAME = "logging.csv"



// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Sensor.newInstance] factory method to
 * create an instance of this fragment.
 */


private val LOCATION = 123

class Sensor : Fragment(), SensorEventListener, LocationListener{

    private lateinit var tvlatitude: TextView
    private lateinit var tvlongitude : TextView

    private lateinit var manager: LocationManager
    private var stringpath: ArrayList<String> = ArrayList()

    //--------------





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
    private lateinit var btnReset: Button

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

    private var param1: String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
     //       param2 = it.getString(ARG_PARAM2)
        }
    }
    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment


        //  initView() --------------------------------------------------------------------------------------
        var view = inflater.inflate(R.layout.fragment_sensor, container, false)


        for (i in idGravity) {
            tvGravity.add(view.findViewById(i))
        }

        for (i in idAcceleration) {
            tvAcceleration.add(view.findViewById(i))
        }
        for (i in idGyro) {
            tvGyro.add(view.findViewById(i))
        }

        tvlatitude = view.findViewById(R.id.latitude)
        tvlongitude = view.findViewById(R.id.longitude)


        btnStart = view.findViewById(R.id.btn_start)
        btnStop = view.findViewById(R.id.btn_stop)
        btnReset = view.findViewById(R.id.btnReset)

        switchgrav = view.findViewById(R.id.gravity_switch)
        switchacce = view.findViewById(R.id.acceleration_switch)
        switchgyro = view.findViewById(R.id.gyro_switch)
        switchlocation = view.findViewById(R.id.location_switch)

        btnStart.isEnabled = true
        btnStop.isEnabled = false
        btnReset.isEnabled = false

       btnStart.setOnClickListener {
            registerListener() // Sensor
             if (switchlocation.isChecked) {
                 getLocation()
            }
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            btnReset.isEnabled = false
        }


        btnStop.setOnClickListener {
            Log.d("Sensors", "Stop button Pressed")
            unregisterListener()
            if (switchlocation.isChecked) {
                manager.removeUpdates(this)

            }
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            btnReset.isEnabled = true

            switchgrav.isActivated = false
            switchacce.isActivated = false
            switchgyro.isActivated = false
            //TODO store the files into the fragment_recording

        }

        btnReset.setOnClickListener {
            gyroX = 0f
            gyroY = 0f
            gyroZ = 0f

            if (switchgrav.isChecked) {

                tvGravity[0].text = "X: 0.00 m/s^2"
                tvGravity[1].text = "Y: 0.00 m/s^2"
                tvGravity[2].text = "Z: 0.00 m/s^2"
            }

            if (switchacce.isChecked) {
                tvAcceleration[0].text = "X: 0.00 m/s^2"
                tvAcceleration[1].text = "Y: 0.00 m/s^2"
                tvAcceleration[2].text = "Z: 0.00 m/s^2"
            }
            if (switchgyro.isChecked) {
                tvGyro[0].text = "X: 0.00 m/s^2"
                tvGyro[1].text = "Y: 0.00 m/s^2"
                tvGyro[2].text = "Z: 0.00 m/s^2"
            }

            if (switchlocation.isChecked) {
                tvlatitude.text = "latitude: 0.00°"
                tvlongitude.text = "longitude: 0.00°"
            }

        }

        // init Sensor


            sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null ) {
                sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null ) {
                sensorAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
            }
            if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null ) {
                sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            }
       return view



    }

    private fun getLocation() {
        manager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 123)
        }
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

    }


    override fun onLocationChanged(location: Location) {
        tvlatitude.text = "latitude: " + "${"%.6f".format(location.latitude)}°"
        tvlongitude.text = "longitude: " + "${"%.6f".format(location.longitude)}°"
        saveLocation(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) +
                "   latitude: ${"%.2f".format(location.latitude)}°" + "  longitude: ${"%.2f".format(location.longitude)}° \n")




    }

    private fun saveLocation(s: String) {
        try {
            var stream: FileOutputStream =
                requireActivity().openFileOutput("Logging_Location.csv", Context.MODE_APPEND)
            stream.use { fos ->
                OutputStreamWriter(fos).use { osw ->
                    osw.write(s)

                }
            }
        }
        catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${requireActivity().filesDir.absolutePath}")

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

private fun registerListener() {

        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null && switchgrav.isChecked) {
            Log.d(TAG, "registerGravity: on")
            sensorManager.registerListener(this, sensorGravity, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "registerGravity: successful")
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null && switchacce.isChecked) {
            Log.d(TAG, "registerAcceleration: on")
            sensorManager.registerListener(this, sensorAcceleration, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "registerAcceleration: successful")
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null && switchgyro.isChecked) {
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
            tvGravity[0].text = "X: ${"%.2f".format(gravityData!!.x1)} m/s^2"
            tvGravity[1].text = "Y: ${"%.2f".format(gravityData!!.x2)} m/s^2"
            tvGravity[2].text = "Z: ${"%.2f".format(gravityData!!.x3)} m/s^2"
            timeGravity = System.currentTimeMillis()

           saveGravity(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) +
                   "   Gravity_x: ${"%.2f".format(gravityData!!.x1)} m/s^2" + " Gravity_y: ${"%.2f".format(gravityData!!.x2)} m/s^2"+ " Gravity_z: ${"%.2f".format(gravityData!!.x2)} m/s^2 \n")
        }
    }

    private fun saveGravity(s: String) {

            // You can find the file in /data/user/0/com.example.sensorlogging/files/Logging.csv

        try {
            var stream: FileOutputStream =
                requireActivity().openFileOutput("Logging_Gravity.csv", Context.MODE_APPEND)
            stream.use { fos ->
                OutputStreamWriter(fos).use { osw -> osw.write(s) }
            }
        }

        catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${requireActivity().filesDir.absolutePath}")

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
            tvAcceleration[0].text = "X: ${"%.2f".format(accelerationData!!.x1)} m/s^2"
            tvAcceleration[1].text = "Y: ${"%.2f".format(accelerationData!!.x2)} m/s^2"
            tvAcceleration[2].text = "Z: ${"%.2f".format(accelerationData!!.x3)} m/s^2"
            timeAcceleration = System.currentTimeMillis()


            saveAcceleration(DateTimeFormatter.ISO_INSTANT.format(Instant.now()) +
                    "   Acc_x: ${"%.2f".format(accelerationData!!.x1)} m/s^2" + " Acc_y: ${"%.2f".format(accelerationData!!.x2)} m/s^2"+ " Acc_z: ${"%.2f".format(accelerationData!!.x2)} m/s^2 \n")

        }

    }



    private fun saveAcceleration(s: String) {
        try {
            var stream: FileOutputStream =
                requireActivity().openFileOutput("Logging_Acceleration.csv", Context.MODE_APPEND)
            stream.use { fos ->
                OutputStreamWriter(fos).use { osw ->
                    osw.write(s)

                }
            }
        }
            catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${requireActivity().filesDir.absolutePath}")

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

                tvGyro[0].text = "X: ${"%.2f".format(gyroData!!.x1*(180.0/Math.PI))} °/s \t\t gyroX: ${"%.2f".format(gyroX*(180.0/Math.PI))} °"
                tvGyro[1].text = "Y: ${"%.2f".format(gyroData!!.x2*(180.0/Math.PI))} °/s \t\t gyroY: ${"%.2f".format(gyroY*(180.0/Math.PI))} °"
                tvGyro[2].text = "Z: ${"%.2f".format(gyroData!!.x3*(180.0/Math.PI))} °/s \t\t gyroZ: ${"%.2f".format(gyroZ*(180.0/Math.PI))} °"
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
            var stream: FileOutputStream =
                requireActivity().openFileOutput("Logging_Gyro.csv", Context.MODE_APPEND)
            stream.use { fos ->
                OutputStreamWriter(fos).use { osw ->
                    osw.write(s)

                }
            }
        }
        catch (ex: IOException) {
            Log.e(TAG, "filesDirectory: ${requireActivity().filesDir.absolutePath}")

        }

    }





    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Recording.
         */

        const val PERMISSION_LOCATION_REQUEST_CODE = 1
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Recording().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }



}