package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.fleks.World
import io.github.fourlastor.jamjam.level.system.LayerComponent
import io.github.fourlastor.jamjam.level.system.RenderSystem
import io.github.fourlastor.ldtk.LDtkReader
import ktx.app.KtxScreen

class LevelScreen : KtxScreen {

    private val mapData = LDtkReader().data(Gdx.files.internal("maps.ldtk").read())
    private val level = LDtkConverter(1f / 16f).convert(mapData.levelDefinitions[0], mapData.defs)

    private val camera = OrthographicCamera().apply { setToOrtho(true) }
    private val viewport = FitViewport(16f, 10f, camera)

    private val world =
        World {
            inject(camera)
            system<RenderSystem>()
        }
            .apply {
                level.layers.forEach { gameLayer ->
                    entity { add<LayerComponent> { layer = gameLayer } }
                }
            }

    override fun show() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        handleInput()
        camera.update()
        world.update(delta)
    }

    private val factor = 1f / 10f

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
        world.dispose()
        level.dispose()
    }
}
