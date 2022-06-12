package io.github.fourlastor.jamjam

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.fourlastor.ldtk.LDtkConverter
import io.github.fourlastor.ldtk.LDtkReader
import ktx.app.KtxScreen
import ktx.app.clearScreen
import ktx.graphics.use

class GameScreen : KtxScreen {

  private val mapData = LDtkReader().data(Gdx.files.internal("maps.ldtk").read())
  private val level = LDtkConverter(1f / 16f).convert(mapData.levelDefinitions[0])

  private val batch = SpriteBatch()
  private val camera = OrthographicCamera().apply { setToOrtho(true) }
  private val viewport = FitViewport(16f, 9f, camera)

  override fun show() {
    Gdx.input.inputProcessor = null
  }

  override fun resize(width: Int, height: Int) {
    viewport.update(width, height, true)
  }

  override fun render(delta: Float) {
    handleInput()
    camera.update()
    clearScreen(0.3f, 0.3f, 0.3f)
    batch.use(viewport.camera) { level.render(it) }
  }

  private val factor = 1f / 16f

  private fun handleInput() {
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      camera.translate(-factor, 0f, 0f)
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      camera.translate(factor, 0f, 0f)
    }
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      camera.translate(0f, -factor, 0f)
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      camera.translate(0f, factor, 0f)
    }

    val effectiveViewportWidth: Float = camera.viewportWidth * camera.zoom
    val effectiveViewportHeight: Float = camera.viewportHeight * camera.zoom
    camera.position.x =
        camera.position.x.coerceIn(effectiveViewportWidth / 2f, 100 - effectiveViewportWidth / 2f)
    camera.position.y =
        camera.position.y.coerceIn(effectiveViewportHeight / 2f, 100 - effectiveViewportHeight / 2f)
  }

  override fun dispose() {
    batch.dispose()
  }
}
