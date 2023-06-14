package com.example.transport

import com.example.transport.bluez.interfaceAddListener
import com.example.transport.bluez.propertyListener
import com.example.transport.impl.Agent1Impl
import com.github.hypfvieh.DbusHelper
import com.github.hypfvieh.bluetooth.DeviceManager
import kotlinx.coroutines.channels.trySendBlocking
import org.bluez.AgentManager1
import org.bluez.Device1
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.handlers.AbstractInterfacesAddedHandler
import org.freedesktop.dbus.handlers.AbstractPropertiesChangedHandler
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.runner.RunWith
import sun.misc.Signal

//@RunWith(AllTests::class)
@RunWith(DynamicSuite::class)
class BluetoothTests {
    companion object {
        private val manager = DeviceManager.createInstance(false)
        val agentManager = DbusHelper.getRemoteObject(
            manager.dbusConnection, "/org/bluez", AgentManager1::class.java
        )

        private var changed: AbstractPropertiesChangedHandler? = null
        private var added: AbstractInterfacesAddedHandler? = null

        //private val adapter = manager.adapter

        @JvmStatic
        fun suite() = if (serverFirst) {
            arrayOf(JoinGameLETest::class.java, CreateGameTest::class.java, JoinGameTest::class.java)
        } else {
            arrayOf(JoinGameLETest::class.java, JoinGameTest::class.java, CreateGameTest::class.java)
        }

        @BeforeClass
        @JvmStatic
        fun makeDiscoverable() {
            println("START")
            println("serverFirst: $serverFirst")

            val btAgent = Agent1Impl(manager.adapter)
            manager.dbusConnection.exportObject(btAgent.objectPath, btAgent)
            agentManager.RegisterAgent(DBusPath(btAgent.objectPath), "")
            agentManager.RequestDefaultAgent(DBusPath(btAgent.objectPath))


            changed = propertyListener {
                when (interfaceName) {
                    Device1::class.java.name -> {
                        val int = manager.dbusConnection.getBluetoothDeviceInt(path)
                        val device = BluetoothDevice(
                            int,
                            manager.adapter,
                            int.objectPath,
                            manager.dbusConnection
                        )
                        val blackList = listOf("UUIDs", "RSSI")
                        propertiesChanged.forEach { (key, value) ->
                            if (key !in blackList) println("${device.name}: ${device.address} -- $key:$value")
                        }

                    }

                }
            }
            manager.registerPropertyHandler(changed)

            added = interfaceAddListener {
                if (interfaces.containsKey(Device1::class.java.name)) {
                    println("source: ${signalSource.path}")
                    interfaces.forEach { (int, variant) ->
                        variant.forEach { (key, v) ->
                            println("$int: $key -- $v")
                        }
                    }
                }
            }
            manager.registerSignalHandler(added)


            Signal.handle(Signal("INT")) { signal ->
                println("Interrupted by Ctrl+C")
                manager.adapter.isDiscoverable = false
                println("exit")
                agentManager.UnregisterAgent(DBusPath(btAgent.objectPath))
                manager.dbusConnection.unExportObject(btAgent.objectPath)
                manager.closeConnection()
                //exitProcess(0)
            }
/*            Runtime.getRuntime().addShutdownHook(Thread() {
                println("shutdown call")
                manager.adapter.isDiscoverable = false
                println("exit")
                agentManager.UnregisterAgent(DBusPath(btAgent.objectPath))
                manager.dbusConnection.unExportObject(btAgent.objectPath)
                manager.closeConnection()
            })*/
        }

        @AfterClass
        @JvmStatic
        fun disableDiscoverability() {
            changed?.let { manager.unRegisterPropertyHandler(it) }
            added?.let { manager.unRegisterSignalHandler(it) }
            println("disable discoverability")
            println("FINISHED")
        }
    }

}