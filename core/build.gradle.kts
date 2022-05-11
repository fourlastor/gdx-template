
plugins {
    kotlin("jvm")
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        ktfmt("0.37")
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    api(libs.gdx)
    api(libs.controllers)
    api(libs.gltf)
    api(libs.ktxActors)
    api(libs.ktxApp)
    api(libs.ktxVis)
}
