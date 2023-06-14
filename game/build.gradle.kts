plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", version = Versions.kotlin))
    testImplementation(kotlin("reflect", version = Versions.kotlin))
    testImplementation("org.junit.jupiter:junit-jupiter:${Versions.junit5}")
}


kotlin {
    sourceSets {
        main {
            resources.srcDir("src/main/kotlin")
        }
        test {
            resources.srcDir("src/test/kotlin")
        }
    }
}

kotlin.jvmToolchain(8)

tasks.test {
    useJUnitPlatform()
}