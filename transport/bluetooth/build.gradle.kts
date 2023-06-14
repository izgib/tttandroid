import com.android.build.api.variant.BuildConfigField
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.CollectingOutputReceiver
import com.example.bluetooth_gradle.BluetoothInfo
import com.example.bluetooth_gradle.BluetoothInfoJvm
import com.github.psxpaul.task.JavaExecFork
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmCompilation


plugins {
    id("com.android.library")
    kotlin("multiplatform") //version Versions.kotlin
    // the most new version is 0.1.15 but it's require gradle 7.+ and jvm 11
    id("com.github.psxpaul.execfork") version "0.2.2"
    id("fixtures-consumer")
    //id("com.example.bluetooth_gradle.plugin") apply false
    //id("com.example.bluetooth_gradle.plugin")
}

buildscript {
    dependencies {
        classpath("com.example.bluetooth_gradle:plugin")
    }
}

repositories {
    mavenCentral()
}


android {
    compileSdkVersion(AndroidVers.sdk)
    buildToolsVersion = AndroidVers.sdkBuild
    defaultConfig {
        minSdkVersion(15)
        targetSdkVersion(AndroidVers.sdk)
        compileSdkVersion(AndroidVers.sdk)
        multiDexEnabled = false
        //versionCode = 1
        //versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArgument("clearPackageData", "true")
        testInstrumentationRunnerArgument("disableAnalytics", "true")
        //testInstrumentationRunner = "com.example.transport.BTTestRunner"

/*        testOptions {
            unitTests.all { test ->
                test.enabled = false
            }
        }*/
    }

    buildTypes {
        val release by buildTypes.getting {
            minifyEnabled(false)
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        val debug by buildTypes.getting {
            isDebuggable = true
            isJniDebuggable = true
            multiDexEnabled = true
            isMinifyEnabled = false
        }
/*        val bluetooth by buildTypes.creating{
            initWith(debug)
            setSigningConfig(debug.signingConfig)
        }
        testBuildType = bluetooth.name*/
    }

    sourceSets {
        val main by sourceSets.getting {
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
            java.srcDirs(
                "src/androidMain/java",
                "src/androidMain/kotlin"
            )
        }
        val androidTest by sourceSets.getting {
            java.srcDir("src/androidAndroidTest/kotlin")
            manifest.srcFile("src/androidAndroidTest/AndroidManifest.xml")
        }
    }

//    compileOptions {
//        isCoreLibraryDesugaringEnabled
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
//        kotlinOptions {
//            jvmTarget = JavaVersion.VERSION_1_8.toString()
//        }
//    }

    packagingOptions {
        exclude("META-INF/LICENSE")
        exclude("META-INF/*.kotlin_module")
    }
}



kotlin {
    targets {
        jvm()
        android() {
            //apply(plugin = "com.android.library")
        }
    }

    targets.all {
        compilations.all {
            kotlinOptions {
                freeCompilerArgs = freeCompilerArgs.plusElement("-Xmulti-platform")
            }
        }
    }
    sourceSets {
        //val commonMain by getting
        val jvmShared by creating {
            dependencies {
                implementation(project(":game"))
                implementation(project(":controllers"))
                implementation(project(":transport:entities")) {
                    exclude("com.google.protobuf", "protobuf-java")
                }
                implementation(project(":transport:bluetooth-proto")) {
                    exclude("com.google.protobuf", "protobuf-java")
                }

                implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
                //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:${Versions.coroutines}")
                //implementation("com.google.protobuf:protobuf-java:${Versions.protobuff}")
                implementation("com.google.protobuf:protobuf-javalite:${Versions.protobuff}")
                implementation("com.google.protobuf:protobuf-kotlin-lite:${Versions.protobuff}") {
                    exclude("com.google.protobuf", "protobuf-java")
                }

                //implementation("com.google.flatbuffers:flatbuffers-java:${Versions.flatbuffers}")
                compileOnly("com.google.code.findbugs:jsr305:3.0.2")
            }
        }

        val jvmSharedTest by creating {
            //dependsOn(jvmShared)
            dependencies {
                //implementation(project(":transport:bluetooth-test"))
                implementation("junit:junit:${Versions.junit4}")
            }
        }

        val jvmMain by getting {
            dependsOn(jvmShared)

            dependencies {
                //implementation("com.github.hypfvieh:dbus-java:3.3.1")
                implementation("com.github.hypfvieh:bluez-dbus:0.1.4")
                implementation("com.rm5248:dbus-java-nativefd:1.0")
                implementation("org.slf4j:slf4j-simple:1.6.1")
                implementation("io.ultreia:bluecove:2.1.1")
            }
        }

        val jvmTest by getting {
            //dependsOn(jvmMain)
            dependsOn(jvmSharedTest)

            dependencies {
                //implementation(project(":transport:bluetooth-test"))
                //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-debug:${Versions.coroutines}")
                //implementation(kotlin("test-junit"))
                //implementation("junit:junit:${Versions.junit4}")
                //implementation(testFixtures(project(":controllers")))
            }
        }
        val androidMain by getting {
            dependsOn(jvmShared)

            dependencies {
                implementation("androidx.core:core-ktx:1.6.0")
                implementation("androidx.fragment:fragment-ktx:1.3.6")
                implementation("no.nordicsemi.android.support.v18:scanner:1.5.1")
            }
        }

        val androidAndroidTest by getting {
            //dependsOn(androidMain)
            dependsOn(jvmSharedTest)

            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
                implementation("androidx.core:core-ktx:1.6.0")
                implementation("androidx.activity:activity-ktx:1.2.2")
                implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
                implementation("androidx.appcompat:appcompat:1.3.1")
                implementation("androidx.test:core-ktx:${Versions.andrTest}")
                implementation("androidx.test:runner:${Versions.andrTest}")
                implementation("androidx.test:rules:${Versions.andrTest}")
                implementation("androidx.test.ext:junit-ktx:${Versions.andrTestExt}")
                implementation("androidx.arch.core:core-testing:${Versions.andrArchCore}")
            }
        }

    }
}

kotlin.jvmToolchain(8)

dependencies {
    //"jvmSharedTestImplementation"(projectCopy(":game"))
    //"jvmSharedTestImplementation"(projectCopy(":controllers"))
    "jvmSharedTestImplementation"(projectFixtures(":controllers"))
    "jvmSharedTestImplementation"(projectFixtures(":transport:network"))
}


abstract class BluetoothInfoAndroidT : BluetoothInfo, DefaultTask() {
    @get:Input
    final override var deviceName: String = ""

    @get:Input
    final override var macAddress: String = ""
}

/*open class BridgeExtension @Inject constructor(objects: ObjectFactory) {
    val bridge: Property<AndroidDebugBridge> = objects.property()
}*/

//val bridge = project.extensions.create("bridge", BridgeExtension::class)

/*configure<BridgeExtension> {
    val conTimeout = 30L
    val discTimeout = 10L
    val timeoutUnit = TimeUnit.SECONDS

    println("here")
    AndroidDebugBridge.initIfNeeded(false)
    bridge.value(AndroidDebugBridge.createBridge(
        android.adbExecutable.path, false,
        conTimeout, timeoutUnit
    )).finalizeValueOnRead()
    println("here2")
    *//*bridge.set(AndroidDebugBridge.createBridge(
        android.adbExecutable.path, false,
        conTimeout, timeoutUnit
    ))*//*
}*/

val getBluetoothInfo by tasks.registering(BluetoothInfoJvm::class)

val getAndroidBluetoothInfoTest by tasks.registering(com.example.bluetooth_gradle.BluetoothInfoAndroid::class) {
    val conTimeout = 30L
    val discTimeout = 5L
    val timeoutUnit = TimeUnit.SECONDS

    enableLocationManager.value(true)
    //grantPairingPermission.value(true)

    doFirst {
        AndroidDebugBridge.initIfNeeded(false)
        val maxRepeats = 3
        bridge.set(run {
            for (i in 0 until maxRepeats) {
                val b = AndroidDebugBridge.createBridge(
                    android.adbExecutable.path, false, conTimeout, timeoutUnit
                ) ?: continue
                if (!b.isConnected) if (!b.restart(conTimeout, timeoutUnit)) continue
                if (b.isConnected) return@run b
            }
            throw IllegalStateException("can not obtain debug bridge in $maxRepeats times")
        }.also {
            println("bridge: ${it.isConnected}")
        })
    }
    doLast {
        println("disconnecting bridge")
        if (!AndroidDebugBridge.disconnectBridge(discTimeout, timeoutUnit)) {
            logger.log(LogLevel.INFO, "can not disconnect debug bridge")
        }
    }
    outputs.upToDateWhen { false }
}


val lol by tasks.registering {
    dependsOn(getAndroidBluetoothInfoTest)
    doFirst {
        println(getAndroidBluetoothInfoTest.get().deviceName)
        println(getAndroidBluetoothInfoTest.get().macAddress)
        println("enabledLocation: ${getAndroidBluetoothInfoTest.get().enableLocationManager.get()}")
    }
}


val getAndroidBluetoothInfo by tasks.registering(BluetoothInfoAndroidT::class) {
    try {
        AndroidDebugBridge.init(false)
    } catch (e: IllegalStateException) {
        if (e.message != "AndroidDebugBridge.init() has already been called.") {
            throw  e
        }
    }

    val conTimeout = 30L
    val discTimeout = 10L
    val timeoutUnit = TimeUnit.SECONDS
    doFirst {
        val bridge = AndroidDebugBridge.createBridge(
            android.adbExecutable.path, false,
            conTimeout, timeoutUnit
        )
        val receiver = CollectingOutputReceiver()

        val device: com.android.ddmlib.IDevice = bridge.devices.let { devices ->
            if (devices.isEmpty()) {
                throw IllegalStateException("devices must be connected")
            }

            var isEmulator = false
            var isInaccessible = false
            for (dev in devices) {
                when {
                    dev.isEmulator -> isEmulator = true
                    !dev.isOnline -> isInaccessible = true
                    dev.isOnline -> return@let dev
                }
            }
            if (isEmulator) {
                if (isInaccessible) {
                    throw IllegalStateException("connected devices are emulators or currently inaccessible")
                }
                throw IllegalStateException("connected device is emulator")
            }
            if (isInaccessible) {
                val pllForm = if (devices.count() > 1) "devices are" else "device is"
                throw IllegalStateException("connected $pllForm currently inaccessible")
            }
            throw IllegalStateException()
        }

        device.executeShellCommand("settings get secure bluetooth_name", receiver)
        device.executeShellCommand("settings get secure bluetooth_address", receiver)

        val name: String
        val address: String

        receiver.output.split("\n").also { output ->
            name = output[0]
            address = output[1]
        }
        if (address == "null") {
            throw IllegalStateException("device: \"$device\" does not support Bluetooth")
        }
        deviceName = name
        macAddress = address
        if (!AndroidDebugBridge.disconnectBridge(discTimeout, timeoutUnit)) {
            println("WTF")
        }
    }
}

/*val androidEnableBluetooth by tasks.registering {
    val conTimeout = 30L
    val discTimeout = 10L
    val timeoutUnit = TimeUnit.SECONDS
    doFirst {
        val bridge = AndroidDebugBridge.createBridge(
            android.adbExecutable.path, false,
            conTimeout, timeoutUnit
        )
        val receiver = CollectingOutputReceiver()

        val device: com.android.ddmlib.IDevice = bridge.devices.let { devices ->
            if (devices.isEmpty()) {
                throw IllegalStateException("devices must be connected")
            }

            var isEmulator = false
            var isInaccessible = false
            for (dev in devices) {
                when {
                    dev.isEmulator -> isEmulator = true
                    !dev.isOnline -> isInaccessible = true
                    dev.isOnline -> return@let dev
                }
            }
            if (isEmulator) {
                if (isInaccessible) {
                    throw IllegalStateException("connected devices are emulators or currently inaccessible")
                }
                throw IllegalStateException("connected device is emulator")
            }
            if (isInaccessible) {
                val pllForm = if (devices.count() > 1) "devices are" else "device is"
                throw IllegalStateException("connected $pllForm currently inaccessible")
            }
            throw IllegalStateException()
        }

        val enableLocationPermission = """SDK=${'$'}(getprop ro.build.version.sdk); if [ $SDK -ge 29 ]; then settings put secure location_mode 3; fi""""

        device.executeShellCommand(enableLocationPermission, receiver)
        device.executeShellCommand("settings get secure bluetooth_address", receiver)

        receiver.awaitCompletion(5, TimeUnit.SECONDS)
        if (!AndroidDebugBridge.disconnectBridge(discTimeout, timeoutUnit)) {
            println("WTF")
        }
    }
}*/

tasks.named<Test>("jvmTest") {
    enabled = true
    useJUnit()
    include("com/example/transport/InMemoryTest*")
    failFast = true
    //testLogging.showStandardStreams = true
    //dependsOn(getBluetoothInfoTest)

    doFirst {
        (getAndroidBluetoothInfo as BluetoothInfo).run {
            systemProperty("deviceName", deviceName)
            systemProperty("macAddress", macAddress)
            systemProperty("serverFirst", 1)

        }
    }
}

androidComponents.selector().withBuildType("debug").apply {

}

androidComponents.finalizeDsl {

}

androidComponents.apply {
    onVariants(selector().withBuildType("debug")) { tests ->
        tests.buildConfigFields.putAll(getBluetoothInfo.map { task ->
            mutableMapOf<String, BuildConfigField<*>>(
                "deviceName" to BuildConfigField(
                    "String",
                    "\"${task.deviceName}\"",
                    "Device name of bluetooth device to connect to"
                ),
                "macAddress" to BuildConfigField(
                    "String",
                    "\"${task.macAddress}\"",
                    "mac address of bluetooth device to connect to"
                ),
                "serverFirst" to BuildConfigField(
                    "byte",
                    1,
                    "Is server testing must be run first?"
                )
            )
        })
    }
}

/*androidComponents.androidTests(
    selector = androidComponents.selector().withBuildType("debug")
) { tests ->
    tests.testedVariant.buildConfigFields.putAll(
        getBluetoothInfo.map { task ->
            mutableMapOf<String, BuildConfigField<*>>(
                "deviceName" to BuildConfigField(
                    "String",
                    "\"${task.deviceName}\"",
                    "Device name of bluetooth device to connect to"
                ),
                "macAddress" to BuildConfigField(
                    "String",
                    "\"${task.macAddress}\"",
                    "mac address of bluetooth device to connect to"
                ),
                "serverFirst" to BuildConfigField(
                    "byte",
                    0,
                    "Is server testing must be run first?"
                )
            )
        }
    )
}*/

val startBluetooth by tasks.registering(JavaExecFork::class) {
    //dependsOn(getAndroidBluetoothInfoTest)
    //println("enabledLocation: ${getAndroidBluetoothInfoTest.get().enableLocationManager.get()}")
    doFirst {
        val androidBluetooth = getAndroidBluetoothInfoTest.get()
        println("enabledLocation: ${androidBluetooth.enableLocationManager.get()}")
        systemProperties(
            "deviceName" to androidBluetooth.deviceName,
            "macAddress" to androidBluetooth.macAddress,
            "serverFirst" to 1
        )
    }

    val compilation = kotlin.targets["jvm"].compilations["test"] as KotlinJvmCompilation
    classpath = compilation.output.allOutputs + compilation.runtimeDependencyFiles

    main = "org.junit.runner.JUnitCore"
    args.add("com.example.transport.BluetoothTests")
    waitForOutput = "FINISHED"
/*    var i = 0
    while (File("/tmp/deamon$i.log").exists()) {
        i++
    }*/
    //standardOutput.set(File("/tmp/deamon$i.log"))
    //errorOutput.set(File("/tmp/deamon-error$i.log"))
    timeout = 100
    killDescendants = true
}

/*val bluetoothPCTest by tasks.registering {
    //dependsOn(getAndroidBluetoothInfoTest)
    dependsOn(
        startBluetooth*//*.apply {
        configure { stopAfter = this@registering }
    }*//*
//        ,"connectedAndroidTest"
    )
    //dependsOn("connectedAndroidTest")
}*/

val bluetoothAndroidTest by tasks.registering{
    dependsOn("connectedAndroidTest")
}
val bluetoothPCTest by tasks.registering {
    dependsOn(startBluetooth, "connectedAndroidTest")
}


/*bluetoothAndroidTest.get().mustRunAfter(getAndroidBluetoothInfoTest.name, "jvmTestClasses")
bluetoothAndroidTest.dependsOn(startBluetooth)
startBluetooth.get().stopAfter = bluetoothAndroidTest*/
/*bluetoothAndroidTest.setMustRunAfter(bluetoothPCTest)
bluetoothAndroidTest.mustRunAfter = bluetoothPCTest*/
println("androidBluetooth registered")



/*afterEvaluate {
    startBluetooth.get().stopAfter = bluetoothPCTest.get()
    tasks.named("connectedAndroidTest") {
        mustRunAfter(startBluetooth)
    }

}*/

/*gradle.taskGraph.whenReady {
    if (hasTask(tasks.getByName("build"))) {
        tasks.getByName("jvmTest").enabled = false
    }
}*/


/*allprojects {
    afterEvaluate {
        project.extensions.findByType<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension>()?.let { kmpExt ->
            kmpExt.sourceSets.removeAll { it.name == "androidAndroidTestRelease"}
        }
    }
}*/
