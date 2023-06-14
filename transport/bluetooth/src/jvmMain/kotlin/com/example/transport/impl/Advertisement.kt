package com.example.transport.impl

import com.example.transport.service.Application
import org.bluez.LEAdvertisement1
import org.freedesktop.dbus.DBusMap
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.exceptions.DBusException
import org.freedesktop.dbus.exceptions.DBusExecutionException
import org.freedesktop.dbus.interfaces.ObjectManager
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.types.DBusMapType
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant

class Advertisement(
//    private val dBusConnection: DBusConnection,
    private val application: Application,
    private val path: String,
    val adType: String = "peripheral"
    //val adType: String = "central"
) : Properties, LEAdvertisement1 {

    private val serviceUUIDs = mutableListOf<String>()
    private val manufacturerData = mutableMapOf<UInt16, ByteArray>()
    private val solicitUUIDs = mutableListOf<String>()
    private val serviceData: MutableMap<String, ByteArray> = mutableMapOf()
    val localName: String = this::class.java.simpleName
    var includeTxPower: Boolean = false
    private val data = mutableMapOf<Byte, ByteArray>()

    val discoverable: Boolean
        get() = getTyped<Boolean>("Discoverable")!!


    private inline fun <reified T> getTyped(propName: String): T? {
        //DbusHelper.getRemoteObject(dBusConnection, objectPath, Properties::class.java).run {
        try {
            Get<Any>(LEAdvertisement1::class.java.name, propName).run {
                if (T::class.java.isAssignableFrom(javaClass)) {
                    return T::class.java.cast(this)
                }
            }
        } catch (e: DBusException) {
            println(e)
        } catch (e: DBusExecutionException) {
            println(e)
        }
        return null
    }

    override fun isRemote(): Boolean = false

    override fun getObjectPath(): String = path

/*    override fun GetManagedObjects(): MutableMap<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>> {
        println("here advertisement manager objects")
        return application.GetManagedObjects()
        return mutableMapOf<DBusPath, MutableMap<String, MutableMap<String, Variant<*>>>>()
    }*/

    override fun Release() {
        println("Advertisement Released: $path")
    }

    fun addServiceUUID(uuids: String) {
        serviceUUIDs.add(uuids)
    }

    fun addSolicitUUID(uuids: String) {
        solicitUUIDs.add(uuids)
    }

    fun addManufacturerData(manufacturerCode: UInt16, data: ByteArray) {
        manufacturerData[manufacturerCode] = data
    }

    fun addServiceData(uuid: String, data: ByteArray) {
        serviceData[uuid] = data
    }

    //fun addData(adType: Byte, data: ByteArray) {
    fun addData(adType: Byte, data: ByteArray) {
        this.data[adType] = data
    }


    override fun <A : Any> Get(interface_name: String, property_name: String): A {
        println("adv get: $property_name")
        TODO()
    }

    override fun <A : Any> Set(interface_name: String, property_name: String, value: A) {
        println("adv set: $property_name:$value")
    }

    override fun GetAll(interface_name: String): MutableMap<String, Variant<*>> {
        //DBusMap(serviceData)
        //Variant(mapOf("8681b031-fb44-441b-bcc7-cb111b242dc7" to byteArrayOf(0x01)), DBusMapType::class.java)
        //Variant(DBusMap(arrayOf(arrayOf(Variant("8681b031-fb44-441b-bcc7-cb111b242dc7"), Variant(byteArrayOf(0x01))))))
        val dataMap = hashMapOf<String, Variant<*>>("8681b031-fb44-441b-bcc7-cb111b242dc7" to Variant(ByteArray(1)))
        //val dataMap = hashMapOf<String, Variant<*>>("8681b031" to Variant(ByteArray(1)))
        return mutableMapOf<String, Variant<*>>(
            "Type" to Variant(adType),
            "ServiceUUIDs" to Variant(application.services.map { it.uuid }.toTypedArray()),
            //"ServiceUUIDs" to Variant(serviceUUIDs.toTypedArray()),
            //"SolicitUUIDs" to Variant(solicitUUIDs.toTypedArray()),
            //"ManufacturerData" to Variant(manufacturerData),
            //"ServiceData" to Variant<Map<String, Variant<*>>>(dataMap, "a{sv}"),
            //"ServiceData" to Variant(mapOf(Variant("8681b031-fb44-441b-bcc7-cb111b242dc7") to Variant(byteArrayOf(0x01)))),
            //"ServiceData" to Variant(arrayOf(arrayOf(Variant("8681b031-fb44-441b-bcc7-cb111b242dc7"), Variant(byteArrayOf(0x01))))),
            //"ServiceData" to Variant(arrayOf(arrayOf(Variant("8681b031-fb44-441b-bcc7-cb111b242dc7"), Variant(d)))),
            //"LocalName" to Variant("game"),
            //"IncludeTxPower" to Variant(includeTxPower),
            //"Data" to Variant(data),
            "Discoverable" to Variant(true),
            //"DiscoverableTimeout" to Variant(UInt16(60)),
            //"Timeout" to Variant(UInt16(60)),
            "Duration" to Variant(UInt16(60)),
            //"Secondary Channel" to Variant("null"),
            //"Includes" to Variant(arrayOf<String>()),
        )
    }
}