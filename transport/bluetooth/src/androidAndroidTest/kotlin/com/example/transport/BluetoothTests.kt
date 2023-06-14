package com.example.transport

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Rule
import org.junit.rules.ExternalResource
import org.junit.runner.RunWith
import kotlin.properties.Delegates


//@RunWith(AllTests::class)
@RunWith(DynamicSuite::class)
class BluetoothTests {
    companion object {
        private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()!!
        private val bluetoothDisabled = when (adapter.state) {
            BluetoothAdapter.STATE_OFF, BluetoothAdapter.STATE_TURNING_OFF -> false
            else -> true
        }
        private var locationEnabled by Delegates.notNull<Boolean>()

        @get:ClassRule
        @JvmStatic
        val initRule = object : ExternalResource() {
            override fun before() {
                setupTesting()
            }

            override fun after() {
                disableAll()
            }
        }

        @get:ClassRule
        @JvmStatic
        val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )

        private fun locationSettings(timeout: Long?) = ActivityScenario.launch(
            Location::class.java, Bundle().apply {
                if (timeout != null) {
                    putLong(Location.TIMEOUT_KEY, timeout)
                }
            }
        )

        @BeforeClass
        @JvmStatic
        fun setupTesting() {
            adapter.enable()

            InstrumentationRegistry.getInstrumentation().targetContext
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationEnabled = LocationManagerCompat.isLocationEnabled(lm)
                if (locationEnabled) {
                    return
                }
                val scenario = locationSettings(5000)
                try {
                    scenario.moveToState(Lifecycle.State.CREATED)
                        .onActivity { activity ->
                            activity.enableLocationProvider()
                        }
                    when (scenario.result.resultCode) {
                        Activity.RESULT_OK -> Unit
                        Activity.RESULT_CANCELED -> IllegalStateException("location service was not enabled")
                        else -> IllegalArgumentException()
                    }
                } finally {
                    scenario.close()
                }
            }
        }

        @JvmStatic
        fun suite(): Array<Class<*>> = if (serverFirst) {
            println("first creation")
            arrayOf(CreateGameLETest::class.java, CreateGameTest::class.java, JoinGameTest::class.java)
        } else {
            println("first joining")
            arrayOf(CreateGameLETest::class.java, JoinGameTest::class.java, CreateGameTest::class.java)
        }

        @JvmStatic
        @AfterClass
        fun disableAll() {
            if (bluetoothDisabled) {
                adapter.disable()
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!locationEnabled) {
                    val scenario = locationSettings(10000)
                    try {
                        scenario.moveToState(Lifecycle.State.CREATED)
                            .onActivity { activity ->
                                activity.enableLocationProvider()
                            }
                    } finally {
                        scenario.close()
                    }
                }
            }
        }
    }
}