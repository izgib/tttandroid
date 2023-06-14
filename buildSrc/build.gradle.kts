plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("fixturesConsumer") {
            id = "fixtures-consumer"
            implementationClass = "FixturesConsumerPlugin"
        }
    }
}


repositories {
    mavenCentral()
    google()
    //jcenter()
}

dependencies {
    //implementation(kotlin("gradle-plugin"))
    //implementation(kotlin("stdlib"))
    val kotlin = "1.7.20"
    val coroutines = "1.6.4"
    val sdkBuild = "30.0.3"

    //implementation(kotlin("stdlib", version = kotlin))
    //implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines")

//    implementation("com.android.tools.ddms:ddmlib:$sdkBuild")
}
