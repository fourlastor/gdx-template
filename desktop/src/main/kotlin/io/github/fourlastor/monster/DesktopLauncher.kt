package io.github.fourlastor.monster

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
  val config =
      Lwjgl3ApplicationConfiguration().apply {
        setWindowedMode(1920, 1080)
        setForegroundFPS(60)
      }
  Lwjgl3Application(MonsterRun(), config)
}
