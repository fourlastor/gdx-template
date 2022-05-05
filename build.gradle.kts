plugins {
    idea
    kotlin("jvm") version "1.6.21"
}

allprojects {
    version = "1.0"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
