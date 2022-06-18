
plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
}

spotless {
    isEnforceCheck = false
    kotlin {
        ktfmt("0.37")
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    api(project(":ldtk"))
    api(libs.controllers)
    api(libs.fleks)
    api(libs.gdx)
    api(libs.gdxAi)
    api(libs.ktxActors)
    api(libs.ktxApp)
    api(libs.ktxGraphics)
    api(libs.ktxVis)
}
