package io.github.fourlastor.monster

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.scenes.scene2d.Stage
import com.kotcrab.vis.ui.VisUI
import ktx.actors.onClick
import ktx.app.KtxScreen
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.actors
import ktx.scene2d.vis.visTable
import ktx.scene2d.vis.visTextButton

class MenuScreen(private val game: MonsterRun) : KtxScreen {

  private val stage = Stage()

  init {
    VisUI.load(VisUI.SkinScale.X2)
    Scene2DSkin.defaultSkin = VisUI.getSkin()
    stage.actors {
      visTable {
        setFillParent(true)
        visTextButton("Start").apply { onClick { game.startGame() } }
      }
    }
    Gdx.input.inputProcessor = stage
  }

  override fun render(delta: Float) {
    stage.act()
    stage.draw()
  }

  override fun dispose() {
    VisUI.dispose()
  }
}
