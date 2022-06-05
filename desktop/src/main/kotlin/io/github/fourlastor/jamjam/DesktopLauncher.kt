package io.github.fourlastor.jamjam

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

fun main() {
  val config =
      Lwjgl3ApplicationConfiguration().apply {
        setWindowedMode(1920, 1080)
        setForegroundFPS(60)
      }
  Lwjgl3Application(JamGame(), config)
}
