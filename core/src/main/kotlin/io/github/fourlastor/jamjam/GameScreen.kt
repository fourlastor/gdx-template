package io.github.fourlastor.jamjam

import com.badlogic.gdx.Gdx
import ktx.app.KtxScreen

class GameScreen : KtxScreen {
    init {
    }

    override fun show() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
    }

    override fun render(delta: Float) {
    }

    override fun dispose() {
    }
}
