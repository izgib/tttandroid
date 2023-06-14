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
    //implementation("com.google.protobuf:protobuf-java:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-javalite:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-kotlin-lite:${Versions.protobuff}") {
        exclude("com.google.protobuf", "protobuf-java")
    }
    //implementation("com.google.flatbuffers:flatbuffers-java:${Versions.flatbuffers}")
    //compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

kotlin.jvmToolchain(8)