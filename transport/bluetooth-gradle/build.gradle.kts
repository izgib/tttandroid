buildscript {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }

    dependencies {
        classpath( "org.jetbrains.kotlin", "kotlin-gradle-plugin", "1.7.20")
        //classpath("com.example.bluetooth_gradle:plugin")
        //classpath("com.netflix.nebula:gradle-lint-plugin:14.2.5")
        //classpath("com.android.tools.build:gradle:4.2.2")
        //classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        //classpath(kotlinModule("gradle-plugin", kotlin_version))
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
    }

    group = "com.example.bluetooth_gradle"
    version = "1.0"

    //apply plugin: 'nebula.lint'
    //gradleLint.rules = ['all-dependency'] // add as many rules here as you'd like
}