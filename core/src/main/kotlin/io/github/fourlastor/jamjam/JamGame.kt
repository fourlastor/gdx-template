package io.github.fourlastor.jamjam

import com.badlogic.gdx.Screen
import io.github.fourlastor.jamjam.level.LevelScreen
import ktx.app.KtxGame

class JamGame : KtxGame<Screen>() {

  override fun create() {
      addScreen(LevelScreen())
      addScreen(MenuScreen(this))
    setScreen<MenuScreen>()
  }

  fun startGame() {
      setScreen<LevelScreen>()
  }
}
