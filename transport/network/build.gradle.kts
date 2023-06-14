plugins {
    //java
    //`java-library`
    kotlin("jvm")
    `java-test-fixtures`
}

repositories {
    mavenCentral()
    //gradlePluginPortal()
}

//java {
//    sourceCompatibility = JavaVersion.VERSION_1_6
//    targetCompatibility = JavaVersion.VERSION_1_6
//}

sourceSets {
    main {
        java.srcDirs(
            //"src/main/java",
            "src/main/kotlin"
        )
    }
    test {
        java.srcDirs(
            //"src/test/java",
            "src/test/kotlin"
        )
    }
}

/*val sharedTests by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    extendsFrom(configurations.testFixturesImplementation.get())
}*/

/*artifacts {
    add("sharedTests", sharedTests)
}*/

tasks.withType<Test> {
    useJUnitPlatform()
    enabled = true
    include("com/example/transport/*")
}

/*kotlin {

    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(JavaVersion.VERSION_1_8.ordinal))
    }
}*/

kotlin.jvmToolchain(8)

/*kotlin.target.compilations.named("test") {
    associateWith(target.compilations.getByName("main"))
}*/



/*val jarTestHelpers = tasks.creating(org.gradle.jvm.tasks.Jar::class) {
    from("$buildDir/inter")
}*/

dependencies {
    implementation(project(":game"))
    implementation(project(":controllers"))
    implementation(project(":transport:entities"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    //implementation("com.google.flatbuffers:flatbuffers-java:${Versions.flatbuffers}")
    //implementation("com.google.flatbuffers:flatbuffers-java-grpc:${Versions.flatbuffers}")
    //implementation(files("libs/flatbuffers-java-grpc-1.12.0.jar"))
    //implementation("com.google.protobuf:protobuf-java:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-javalite:${Versions.protobuff}")
    implementation("com.google.protobuf:protobuf-kotlin-lite:${Versions.protobuff}") {
        exclude("com.google.protobuf", "protobuf-java")
    }
    "io.grpc:grpc".also { grpc ->
        val grpcVersion = "1.51.1"
        implementation("$grpc-okhttp:$grpcVersion")
        implementation("$grpc-protobuf-lite:$grpcVersion") {
            exclude("com.google.protobuf", "protobuf-java")
        }
        implementation("$grpc-stub:$grpcVersion")
        implementation("$grpc-kotlin-stub:1.3.0")

        testImplementation("$grpc-testing:$grpcVersion")
    }
    //implementation("com.google.api.grpc:proto-google-common-protos:2.11.0")

    compileOnly("com.google.code.findbugs:jsr305:3.0.2")


    testFixturesImplementation(project(":game"))
    testFixturesImplementation(testFixtures(project(":controllers")))

    testImplementation(project(":game"))
    testImplementation(testFixtures(project(":controllers")))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
    //implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
}

/*
val testFixturesJar by tasks.existing(Jar::class)

val sharedJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    //extendsFrom(configurations.testFixturesRuntimeElements.get())
}

artifacts {
    val file = testFixturesJar.get().outputs.files.singleFile
    println("file: ${file.name}")
    add(sharedJars.name, file) {
        builtBy(testFixturesJar)
    }
}*/
