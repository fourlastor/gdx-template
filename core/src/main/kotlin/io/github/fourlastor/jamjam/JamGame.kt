package io.github.fourlastor.jamjam

import com.badlogic.gdx.Screen
import ktx.app.KtxGame

class JamGame : KtxGame<Screen>() {

  override fun create() {
    addScreen(GameScreen())
    addScreen(MenuScreen(this))
    setScreen<MenuScreen>()
  }

  fun startGame() {
    setScreen<GameScreen>()
  }
}
