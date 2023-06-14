plugins {
    `java-gradle-plugin`
    kotlin("jvm")
}


repositories {
    mavenCentral()
    google()
}


gradlePlugin {
    val bluetoothPlugin by plugins.creating {
        id = "com.example.bluetooth_gradle.plugin"
        implementationClass = "com.example.bluetooth_gradle.BluetoothPlugin"
        version = "1.0"
    }
}

dependencies {
    val kotlin = "1.7.20"
    val coroutines = "1.6.4"
    val sdkBuild = "30.0.3"

    implementation(kotlin("stdlib", version = kotlin))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")
    implementation("com.android.tools.ddms:ddmlib:$sdkBuild")
}
/*java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}*/

