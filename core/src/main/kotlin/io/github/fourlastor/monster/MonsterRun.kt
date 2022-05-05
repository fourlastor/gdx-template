package io.github.fourlastor.monster

import com.badlogic.gdx.Screen
import ktx.app.KtxGame


class MonsterRun : KtxGame<Screen>() {

    override fun create() {
        addScreen(TestScreem())
        setScreen<TestScreem>()
    }
}
