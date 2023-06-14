import com.android.build.gradle.internal.packaging.fromProjectProperties
import com.android.build.gradle.internal.tasks.factory.dependsOn
import org.gradle.api.tasks.bundling.Jar

plugins {
    `java-library`
    `java-test-fixtures`
    kotlin("jvm")
/*    id("com.android.library")
    kotlin("android")*/
}

tasks.withType<Test> {
    useJUnitPlatform()
    //enabled = true
    //include("com/example/controllers/models/*")
}

repositories {
    mavenCentral()
}


/*
android {
    compileSdkVersion(AndroidVers.sdk)
    buildToolsVersion = AndroidVers.sdkBuild
    defaultConfig {
        minSdkVersion(18)
        multiDexEnabled = false
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()

    packagingOptions {
        exclude("META-INF/LICENSE")
    }

    sourceSets {
        named("main") {
            java.srcDir("src/main/kotlin")
        }
        named("test") {
            java.srcDir("src/test/kotlin")
        }
    }
}
*/

/*val testFixturesJar by tasks.existing(Jar::class)

val sharedJars by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
}*/

dependencies {
    implementation(project(":game"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")

    testFixturesImplementation(project(":game"))
    testFixturesImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
    //sharedJars(files(testFixturesJar.get().outputs.files.singleFile))
}

kotlin.jvmToolchain(8)

/*artifacts {
    val file = testFixturesJar.get().outputs.files.singleFile
    println("file: ${file.name}")
    add(sharedJars.name, file) {
        builtBy(testFixturesJar)
    }
}*/

/*kotlin.target.compilations.named("test") {
    associateWith(target.compilations.getByName("main"))
}*/

/*tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}*/

//val jarTests by tasks.registering(Jar::class) {
//    dependsOn(tasks.getByName("assembleDebugUnitTest"))
//    archiveClassifier.set("tests")
//    from("$buildDir/tmp/kotlin-classes/debugUnitTest")
//    exclude(
//            "**/*Test.class",
//            "**/*TestKt.class",
//            "**/*Test_*.class"
//    )
//    /*    exclude(
//            "**R.class",
//            "**R$*.class"
//    )*/
//    includeEmptyDirs = false
//}
//
//val unitTestArtifact: Configuration by configurations.creating {
//    isCanBeResolved = false
//    isCanBeConsumed = true
//    artifacts {
//        add(this@creating.name, jarTests)
//    }
//}
