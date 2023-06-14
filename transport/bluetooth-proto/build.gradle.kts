plugins {
    kotlin("jvm")
}


repositories {
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    main {
        java.srcDirs(
            "src/$name/java",
            "src/$name/kotlin"
        )
    }
}

dependencies {
    implementation(project(":transport:entities"))
    //implementation("com.google.protobuf:protobuf-java:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-javalite:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-kotlin-lite:${Versions.protobuff}") {
        exclude("com.google.protobuf", "protobuf-java")
    }
}

kotlin.jvmToolchain(8)

/* This project is workaround for inability to use java plugin in kotlin
    multiplatform(jvm/android) project */
