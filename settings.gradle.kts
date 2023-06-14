rootProject.name = "Tic-Tac-Toe"
rootProject.buildFileName = "build.gradle.kts"
//include ':app', ':domain', ':networking', ':controllers'
include(":game", ":controllers", ":transport:entities", ":transport:network" //":app"
    , ":transport:bluetooth" , ":app"
)



pluginManagement {
    val gradle_android_version by extra("7.2.0")

    repositories {
        //project(":transport:bluetooth-gradle")
/*        maven {
            url = uri("transport/bluetooth-gradle")
        }*/
/*        includeBuild("transport/bluetooth-gradle") {
            dependencySubstitution {
                substitute(module("com.example:bluetooth_gradle")).with(project(":plugin"))
            }
        }*/
        gradlePluginPortal()
        mavenCentral()
        google()
        jcenter()
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id.startsWith("com.android.")) {
                val versionPart = if (requested.version == null) ":$gradle_android_version" else ":${requested.version}"
                useModule("com.android.tools.build:gradle${versionPart}")
                //useModule("com.android.tools.build:gradle:${requested.version}")
            }
        }
    }
}

/*plugins {
    id("com.android.application") apply false
}*/
includeBuild("transport/bluetooth-gradle")/* {
    dependencySubstitution {
        substitute(module("com.example.bluetooth_gradle:plugin:1.0")).with(project(":plugin"))
    }
}*/


include(":transport:bluetooth-proto")
//include(":transport:bluetooth-gradle")
