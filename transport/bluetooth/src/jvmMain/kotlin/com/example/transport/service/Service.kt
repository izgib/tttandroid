package com.example.transport.service

import com.example.controllers.ClientResponse
import com.example.controllers.GameSettings
import com.example.controllers.models.InterruptCause
import com.example.game.Coord
import com.example.game.GameState
import com.example.game.Mark
import com.example.transport.*
import com.example.transport.extensions.toClientResponse
import com.example.transport.extensions.toGameParams
import com.example.transport.extensions.toStopCause
import com.example.transport.impl.Characteristic
import com.example.transport.impl.Descriptor
import com.example.transport.impl.Service
import jnr.unixsocket.UnixSocketChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.launch
import org.bluez.GattCharacteristic1
import org.bluez.GattService1
import org.bluez.datatypes.TwoTuple
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.FileDescriptor
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.interfaces.Properties
import org.freedesktop.dbus.messages.Message
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.Variant
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

class GameService(private val connection: DBusConnection) :
    Service(uuid = BluetoothLEInteractor.service.toString(), true),
    GattService1,
    Properties {
    val settings: SettingsCharacteristic = SettingsCharacteristic(
        this,
        GameSettings(3, 3, 3, Mark.Cross)
    )

    val clientMessages = ClientMessageCharacteristic(this)
    val serverMessages = ServerMessageCharacteristic(connection, this)
    private val lolChar = LolCharacteristic(this)

    init {
        addCharacteristic(settings)
        addCharacteristic(clientMessages)
        addCharacteristic(serverMessages)
        //addCharacteristic(lolChar)
    }
}

class SettingsCharacteristic(service: Service, var settings: GameSettings) : Characteristic(
    0,
    BluetoothLEInteractor.settings.toString(),
    listOf("broadcast", "read"), //, "read"),
    service,
    UInt16(0)
), GattCharacteristic1, Properties {
    val settingsData
        get() = settings.toGameParams().toByteArray()

    init {
        //addDescriptor(ServerCharacteristicConfiguration(this))
    }

    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        println("try read settings")
        if (_options!!.isNotEmpty()) {
            _options.forEach { option, v ->
                println("with option: $option: $v")
            }
        }
        return settingsData
    }

/*    override fun <A> Set(interface_name: String, property_name: String, value: A) {
        super.Set(interface_name, property_name, value)
    }*/
}

/*class LolCharacteristic(service: Service) : Characteristic(
    3, "55555555-5555-5555-5555-555555555555",
    listOf("read"),
    service,
) {
    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        println("try read settings")
        if (_options!!.isNotEmpty()) {
            _options.forEach { option, v ->
                println("with option: $option: $v")
            }
        }
        return byteArrayOf(0x00, 0x01)
    }
}*/

class LolCharacteristic(val service: Service) : GattCharacteristic1, Properties {
    val flags = listOf("read")
    private val uuid = "55555555-5555-5555-5555-555555555555"

    private val path: String

    init {
        path = "${Service.path}/char3"
    }


    override fun isRemote(): Boolean {
        return super<GattCharacteristic1>.isRemote()
    }

    override fun getObjectPath(): String {
        return path
    }

    override fun <A : Any?> Get(interface_name: String?, property_name: String?): A {
        TODO("Not yet implemented")
    }

    override fun <A : Any?> Set(interface_name: String?, property_name: String?, value: A) {
        TODO("Not yet implemented")
    }

    override fun GetAll(interface_name: String?): MutableMap<String, Variant<*>> {
        val res = mutableMapOf<String, Variant<*>>(
            "Service" to Variant(DBusPath(service.objectPath)),
            "UUID" to Variant(uuid),
            "Flags" to Variant(flags.toTypedArray()),
            //"Value" to Variant(byteArrayOf(127))
            //"Descriptors" to Variant(arrayOf<String>())
            //"Includes" to Variant(arrayOf(DBusPath(charPath))),
            //"Characteristics" to Variant(arrayOf(DBusPath(charPath))),
            //"Includes" to Variant(arrayOf<String>()),
        )
        println(res)

        return res
    }

    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        println("try read settings")
        if (_options!!.isNotEmpty()) {
            _options.forEach { option, v ->
                println("with option: $option: $v")
            }
        }
        return byteArrayOf(0x00, 0x01)
    }

    override fun WriteValue(_value: ByteArray?, _options: MutableMap<String, Variant<*>>?) {
        TODO("Not yet implemented")
    }

    override fun AcquireWrite(_options: MutableMap<String, Variant<*>>?): TwoTuple<FileDescriptor, UInt16> {
        TODO("Not yet implemented")
    }

    override fun AcquireNotify(_options: MutableMap<String, Variant<*>>?): TwoTuple<FileDescriptor, UInt16> {
        TODO("Not yet implemented")
    }

    override fun StartNotify() {
        TODO("Not yet implemented")
    }

    override fun StopNotify() {
        TODO("Not yet implemented")
    }

    override fun Confirm() {
        TODO("Not yet implemented")
    }
}

class ClientMessageCharacteristic(service: Service) : Characteristic(
    1, BluetoothLEInteractor.clientMessages.toString(),
    listOf("write"),
    service, handle = UInt16(0)
), GattCharacteristic1, Properties {
    val response = Channel<ClientResponse>()
    private var socket: UnixSocketChannel? = null

    init {
        /*addDescriptor(
            ProtobufDescriptor(
                0, "497c25e6-9f41-44d0-8a82-33425ce9964b",
                listOf("write"), this
            )
        )*/
    }

    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        println("${this.javaClass.name}: try to read")
        return byteArrayOf()
    }

    override fun WriteValue(_value: ByteArray, _options: MutableMap<String, Variant<*>>) {
        println("written value: ${_value.map { String.format("%02x", it)}}")
        _options.forEach { key, value ->
            println("$key: $value")
        }
        val offset = _options["offset"]
        response.trySend(ClientMessage.parseFrom(_value).toClientResponse())
    }

    private fun connectSocket() {
        val selector = Selector.open()
        //socket!!.configureBlocking(false)
        socket!!.register(selector, SelectionKey.OP_READ)
        val buffer = ByteBuffer.allocate(256)

        println("here 1")
        CoroutineScope(Dispatchers.IO).launch {
            println("here 2")
            socket!!.read(buffer)
            println("here 3")
            response.trySend(ClientMessage.parseFrom(buffer).toClientResponse())
            println("here 4")
            buffer.clear()
/*            selector.select()
            val selectedKeys = selector.selectedKeys()*/
        }
    }

    override fun AcquireWrite(_options: MutableMap<String, Variant<*>>): TwoTuple<FileDescriptor, UInt16> {
        _options.forEach { key, _ ->
            println(key)
        }
        val device: Variant<DBusPath> = _options.get("device") as Variant<DBusPath>
        println("acquire write: $device")
        val mtu: Int = ((_options.get("mtu") as Variant<UInt16>).value as UInt16).toInt()
        val link = _options.get("link")
        println("mtu: $mtu $link")
        socket = UnixSocketChannel.open()
        connectSocket()
        return TwoTuple(FileDescriptor(socket!!.fd), UInt16(mtu))
    }

    override fun GetAll(interface_name: String?): MutableMap<String, Variant<*>> {
        val settings = getDefaultProperties()
        //settings["WriteAcquired"] = Variant(true)
        //settings["NotifyAcquired"] = Variant(true)
        return settings
    }

    override fun Confirm() {
        println("client confirm")
    }
}

class ServerMessageCharacteristic(val connection: DBusConnection, service: Service) :
    Characteristic(
        2, BluetoothLEInteractor.serverMessages.toString(),
        listOf("read", "notify"),
        service,
        notifying = false,
        handle = UInt16(0)
    ), GattCharacteristic1, Properties {
    private var socket: UnixSocketChannel? = null
    private val byteBuffer = ByteBuffer.allocate(512)
    var onNotify: ((notify: Boolean) -> Unit)? = null
    private var internalValue: ByteArray? = null
    private var notifying: Boolean? = null

    private var value: ByteArray
        get() = ByteArray(0)
        set(v) {
            println("try to send")
            internalValue = v

            connection.sendMessage(Properties.PropertiesChanged(objectPath, GattCharacteristic1::class.java.name,
                mapOf("Value" to Variant(v)), listOf()
            ))
            //Set(GattCharacteristic1::javaClass.name, "Value", v)
        }

    init {

    }


    override fun ReadValue(_options: MutableMap<String, Variant<*>>?): ByteArray {
        println("${this.javaClass.name}: try to read")
        return byteArrayOf()
    }

    override fun AcquireNotify(_options: MutableMap<String, Variant<*>>): TwoTuple<FileDescriptor, UInt16> {
        val device: Variant<DBusPath> = _options.get("device") as Variant<DBusPath>
        println("acquire notify: $device")
        val mtu: Short = ((_options.get("mtu") as Variant<UInt16>).value as UInt16).toShort()
        val link = _options.get("link")
        socket = UnixSocketChannel.create()

        return TwoTuple(FileDescriptor(socket!!.fd), UInt16(mtu - 1)).also {
            //onNotify?.let { notify -> notify(true) }
        }
    }

    override fun StartNotify() {
        println("start notifying")
        onNotify?.let { notify -> notify(true) }
        notifying = true
        Properties.PropertiesChanged(objectPath, GattCharacteristic1::class.java.name,
            mapOf("Notifying" to Variant(notifying)), listOf()
        )
    }

    override fun StopNotify() {
        println("stop notifying")
        onNotify?.let { notify -> notify(false) }
        socket?.close()
    }

    override fun Confirm() {
        println("confirm")
    }

/*    override fun <A> Set(interface_name: String, property_name: String, value: A) {
        println("$property_name has set with $value by $interface_name")
    }*/

    override fun GetAll(interface_name: String?): MutableMap<String, Variant<*>> {
        val settings = getDefaultProperties()
        if (notifying != null) {
            settings["Notifying"] = Variant(notifying)
        }
        if (internalValue != null) {
            settings["Value"] = Variant(internalValue)
        }
        if (handle != null) {
            settings["Handle"] = Variant(handle)
        }
        //settings["NotifyAcquired"] = Variant(true)
        return settings
    }

    fun sendInterruption(cause: InterruptCause) {
        val data = bluetoothCreatorMsg {
            this.cause = cause.toStopCause()
        }.toByteArray()
        value = data
        //socket!!.write(byteBuffer.put(data))
    }

    fun sendTurn(move: Coord?, state: GameState) {
        println("sending turn")
        value = turnToCreatorMsg(move, state).toByteArray()
        //socket!!.write(byteBuffer.put(turnToCreatorMsg(move, state).toByteArray()))
    }
}

class ServerCharacteristicConfiguration(characteristic: Characteristic) : Descriptor(
    0, "2903",
    listOf("read", "secure-write"), characteristic
) {

    override fun ReadValue(_flags: MutableMap<String, Variant<*>>?): ByteArray {
        println("try read SCCD")
        if (_flags!!.isNotEmpty()) {
            _flags.forEach { option, v ->
                println("with option: $option: $v")
            }
        }
        return byteArrayOf(0x0001)
    }
}

class ProtobufDescriptor(
    index: Int,
    uuid: String,
    flags: List<String>,
    characteristic: Characteristic
) : Descriptor(index, uuid, flags, characteristic) {

}

private fun Mark.toProtoMark(): MarkType {
    return when (this) {
        Mark.Cross -> MarkType.MARK_TYPE_CROSS
        Mark.Nought -> MarkType.MARK_TYPE_NOUGHT
        Mark.Empty -> MarkType.MARK_TYPE_UNSPECIFIED
    }
}