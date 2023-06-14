plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-parcelize")
    //id("com.getkeepsafe.dexcount")
}


buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }
    /*dependencies {
        classpath 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.8.6'
    }*/
}

android {
    compileSdkVersion(AndroidVers.sdk)
    buildToolsVersion = AndroidVers.sdkBuild
    defaultConfig {
        applicationId = "com.example.game.tic_tac_toe"
        minSdkVersion(19)
        multiDexEnabled = false
        versionCode = 1
        versionName = "1.0"
        targetSdkVersion(AndroidVers.sdk)
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            //minifyEnabled = false
            multiDexEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            applicationIdSuffix = ".test"
            //debuggable(true)
            //jniDebuggable(true)
            //minifyEnabled(false)
            multiDexEnabled = true
        }
    }

    buildFeatures.viewBinding = true

    android {
        lint {
            baseline = file("lint-baseline.xml")
        }
    }


/*    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }*/

    packagingOptions {
        exclude("META-INF/LICENSE")
    }
}


dependencies {
    implementation(project(":game"))
    implementation(project(":controllers"))
    implementation(project(":transport:network"))
    implementation(project(":transport:bluetooth"))

    val lifecycle_version = "2.4.0-alpha02"
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.activity:activity-ktx:1.2.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")

    //implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.6.0")
    //implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.3.6")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}")
    //implementation("com.stepstone.stepper:material-stepper:4.0.0")
    implementation("com.appyvet:materialrangebar:1.4.6")
    implementation("com.google.android.material:material:1.3.0")
    implementation("com.github.Zhuinden:simple-stack:2.4.0")
    testImplementation("junit:junit:${Versions.junit4}")
}