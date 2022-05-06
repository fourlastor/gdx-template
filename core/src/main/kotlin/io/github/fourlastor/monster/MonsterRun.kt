package io.github.fourlastor.monster

import com.badlogic.gdx.Screen
import ktx.app.KtxGame


class MonsterRun : KtxGame<Screen>() {

    override fun create() {
        addScreen(MenuScreen(this))
        addScreen(TestScreen())
        setScreen<MenuScreen>()
    }

    fun startGame() {
        setScreen<TestScreen>()
    }
}
