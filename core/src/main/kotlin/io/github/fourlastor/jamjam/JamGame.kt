package io.github.fourlastor.jamjam

import com.badlogic.gdx.Screen
import com.badlogic.gdx.physics.box2d.Box2D
import io.github.fourlastor.jamjam.level.LevelScreen
import ktx.app.KtxGame

class JamGame : KtxGame<Screen>() {

    override fun create() {
        Box2D.init()
        addScreen(LevelScreen())
        addScreen(MenuScreen(this))
        setScreen<LevelScreen>()
    }

    fun startGame() {
        setScreen<LevelScreen>()
    }
}
