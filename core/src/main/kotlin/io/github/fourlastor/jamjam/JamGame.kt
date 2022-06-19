package io.github.fourlastor.jamjam

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.physics.box2d.Box2D
import io.github.fourlastor.jamjam.level.LevelScreen
import io.github.fourlastor.ldtk.LDtkReader
import ktx.app.KtxGame

class JamGame : KtxGame<Screen>() {

    private val reader = LDtkReader()

    override fun create() {
        Box2D.init()
        val gameData = reader.data(Gdx.files.internal("maps.ldtk").read())

        addScreen(LevelScreen())
        addScreen(MenuScreen(this, gameData))
        setScreen<MenuScreen>()
    }

    fun startGame() {
        setScreen<LevelScreen>()
    }
}
