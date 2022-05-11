package io.github.fourlastor.monster

import com.badlogic.gdx.Screen
import io.github.fourlastor.monster.editor.EditorScreen
import ktx.app.KtxGame


class MonsterRun : KtxGame<Screen>() {

    override fun create() {
        addScreen(EditorScreen())
        setScreen<EditorScreen>()
    }

    fun startGame() {
        setScreen<TestScreen>()
    }
}
