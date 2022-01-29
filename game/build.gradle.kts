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
