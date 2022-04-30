package io.github.fourlastor.monster

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

object DesktopLauncher {
    @JvmStatic
    fun main(vararg args: String) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            setForegroundFPS(60)
        }
        Lwjgl3Application(MonsterRun(), config)
    }
}