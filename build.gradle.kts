// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build", "gradle", AndroidVers.gradleVersion)
        classpath( "org.jetbrains.kotlin", "kotlin-gradle-plugin", Versions.kotlin)
        //classpath("com.example.bluetooth_gradle:plugin")
        //classpath("com.netflix.nebula:gradle-lint-plugin:14.2.5")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        //classpath(kotlinModule("gradle-plugin", kotlin_version))
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        jcenter()
    }

    //apply plugin: 'nebula.lint'
    //gradleLint.rules = ['all-dependency'] // add as many rules here as you'd like
}

repositories {
    mavenCentral()
}
