plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta7"
}

val junitVersion = "5.11.4"

group = "com.zenmo.shadowjunit"
version = junitVersion
description = "Creates a fat jar of JUnit so it can be easily imported in AnyLogic"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.junit:junit-bom:$junitVersion"))
    implementation("org.junit.jupiter:junit-jupiter")
    implementation("org.junit.platform:junit-platform-launcher")
}

tasks.shadowJar {
    archiveBaseName = "junit-shadow"
    archiveClassifier = ""
    archiveVersion = junitVersion
}
