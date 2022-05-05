plugins {
    id("application")
    kotlin("jvm")
    application
}

group = "io.github.fourlastor"
version = "1.0"

application {
    mainClass.set("io.github.fourlastor.monster.DesktopLauncherKt")
}

@Suppress("UnstableApiUsage")
dependencies {
    api(project(":core"))
    api("com.badlogicgames.gdx:gdx-platform:${libs.versions.gdx.get()}:natives-desktop")
    api(libs.gdxBackendLwjgl3)
    api(libs.controllersDesktop)
}