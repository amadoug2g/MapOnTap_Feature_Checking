package agc.playground.gpschecker

import agc.playground.gpschecker.databinding.ActivityMainBinding
import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

class MainActivity : AppCompatActivity() {

    //region Variables
    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 5
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var binding: ActivityMainBinding
    private var errorMessage: String = ""
    private var isAccelerometerAvailable = false
    private var isLightAvailable = false
    private var isGyroscopeAvailable = false
    private var isMagnetAvailable = false

    // Device information
    private var deviceHeight = ""
    private var deviceWidth = ""
    private var deviceVersionRelease = ""
    private var deviceVersionCodename = ""
    private var deviceVersionSdkInt = ""
    //endregion

    //region Override Methods
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        setupViews()

        binding.compatibilityText.visibility = View.INVISIBLE
    }
    //endregion

    //region Setup
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun setupViews() {

        deviceData()

        binding.gpsBtn.setOnClickListener {
            binding.compatibilityText.apply {
                visibility = View.VISIBLE

                text =
                    if (checkPlayServices())
                        "Google Play Services is available on your device"
                    else
                        "Google Play Services is NOT compatible with your device"

                displayError()
            }
        }

        binding.sensorBtn.setOnClickListener {
            checkSensors()
        }
    }
    //endregion

    //region Device Info
    @SuppressLint("SetTextI18n")
    private fun deviceData() {
        deviceVersionRelease = Build.VERSION.RELEASE
        deviceVersionCodename = Build.VERSION.CODENAME
        deviceVersionSdkInt = "${Build.VERSION.SDK_INT}"

        val fields = Build.VERSION_CODES::class.java.fields
        val codeName = fields.firstOrNull { it.getInt(Build.VERSION_CODES::class) == Build.VERSION.SDK_INT }?.name ?: "UNKNOWN"

        binding.deviceVersionReleaseText.text = "Android Version: $deviceVersionRelease ($codeName)"
        binding.deviceSdkText.text = "SDK version: $deviceVersionSdkInt"
    }
    //endregion

    //region Play Google Services
    private fun checkPlayServices(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailability.isGooglePlayServicesAvailable(this)

        return when (apiAvailability.isGooglePlayServicesAvailable(this)) {
            ConnectionResult.SUCCESS -> {
                Log.i("TAG", "The connection result is SUCCESS")
                true
            }
            ConnectionResult.SERVICE_MISSING -> {
                Log.i("TAG", "The connection result is MISSING")


                try {
                    apiAvailability.getErrorDialog(
                        this,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST
                    )!!
                        .show()
                } catch (e: Exception) {
                    Log.w("TAG", "Error: $e")
                }


                errorMessage = "Google Play services is missing on this device."
                false
            }
            ConnectionResult.SERVICE_UPDATING -> {
                Log.i("TAG", "The connection result is UPDATING")

                errorMessage = "Google Play service is currently being updated on this device."
                false
            }
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                Log.i("TAG", "The connection result is VERSION UPDATE REQUIRED")

                errorMessage = "The installed version of Google Play services is out of date."
                false
            }
            ConnectionResult.SERVICE_DISABLED -> {
                Log.i("TAG", "The connection result is DISABLED")

                errorMessage =
                    "The installed version of Google Play services has been disabled on this device."
                false
            }
            ConnectionResult.SERVICE_INVALID -> {
                Log.i("TAG", "The connection result is INVALID")

                errorMessage =
                    "The version of the Google Play services installed on this device is not authentic."
                false
            }
            else -> {
                Log.i(
                    "TAG",
                    "(else) The connection result is ${
                        apiAvailability.isGooglePlayServicesAvailable(this)
                    }"
                )

                errorMessage = "Result is ${apiAvailability.isGooglePlayServicesAvailable(this)}"
                false
            }
        }
    }
    //endregion

    //region Sensors
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkSensors() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        binding.apply {
            sensorAccelerometerText.apply {
                visibility = View.VISIBLE

                text =
                    if (checkAccelerometer())
                        "Accelerometer is available."
                    else
                        "Accelerometer is NOT available."

            }

            sensorGyroscopeText.apply {
                visibility = View.VISIBLE

                text =
                    if (checkGyroscope())
                        "Gyroscope is available."
                    else
                        "Gyroscope is NOT available."

            }

            sensorLightText.apply {
                visibility = View.VISIBLE

                text =
                    if (checkLight())
                        "Light sensor is available."
                    else
                        "Light sensor is NOT available."

            }

            sensorMagnetText.apply {
                visibility = View.VISIBLE

                text =
                    if (checkMagnet())
                        "Magnet sensor is available."
                    else
                        "Magnet sensor is NOT available."

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkAccelerometer(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) != null
    }

    private fun checkGyroscope(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null
    }

    private fun checkLight(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null
    }

    private fun checkMagnet(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null
    }
    //endregion

    //region View Methods
    @SuppressLint("SetTextI18n")
    private fun displayError() {
        binding.errorText.apply {
            if (errorMessage.isNotEmpty()) {
                visibility = View.VISIBLE
                text = "An error occurred: $errorMessage"
            } else {
                visibility = View.INVISIBLE
            }
        }
    }
    //endregion
}