package com.example.transport

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.location.LocationManagerCompat


// launch Location Settings Activity to enable service that returns OK then service were enabled
// within specified TIMEOUT and returns CANCELED otherwise
class Location : ComponentActivity() {
    //val countDownLatch = CountDownLatch(1)
    private lateinit var lm: LocationManager
    private lateinit var timer: CountDownTimer
    private val locationPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            println("returning result: ${result.resultCode}")
            timer.cancel()
            setResult(
                if (LocationManagerCompat.isLocationEnabled(lm)) {
                    Activity.RESULT_OK
                } else {
                    Activity.RESULT_CANCELED
                }
            )
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val timeout = intent.getLongExtra(TIMEOUT_KEY, DEFAULT_TIMEOUT)
        println("timeout: $timeout")
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        timer = object : CountDownTimer(timeout, 1000) {
            override fun onTick(millisUntilFinished: Long) = Unit
            override fun onFinish() {
                println("finished")
                finishActivity(Activity.RESULT_CANCELED)
            }
        }
        timer.start()
        println("timer started")
    }


    fun enableLocationProvider() {
        // there is using FLAG_ACTIVITY_NEW_TASK flag because of ActivityScenario delays
        // in detail https://github.com/android/android-test/issues/676
        locationPermission.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            //addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    companion object {
        private const val DEFAULT_TIMEOUT = 15000L
        const val TIMEOUT_KEY = "LOCATION_SETTINGS_TIMEOUT"
    }
}