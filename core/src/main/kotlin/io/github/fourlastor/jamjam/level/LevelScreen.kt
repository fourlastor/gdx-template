package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.fleks.World
import io.github.fourlastor.jamjam.JamGame
import io.github.fourlastor.jamjam.level.system.BodiesListener
import io.github.fourlastor.jamjam.level.system.Box2dComponent
import io.github.fourlastor.jamjam.level.system.InputSystem
import io.github.fourlastor.jamjam.level.system.LayerComponent
import io.github.fourlastor.jamjam.level.system.PhysicsDebugSystem
import io.github.fourlastor.jamjam.level.system.PhysicsSystem
import io.github.fourlastor.jamjam.level.system.RenderSystem
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLevelDefinition
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import ktx.graphics.center

class LevelScreen(
    private val game: JamGame,
    private val levelDefinition: LDtkLevelDefinition,
    definitions: Definitions
) : KtxScreen {

    private val level = LDtkConverter(1f / 16f).convert(this.levelDefinition, definitions)

    private val camera = OrthographicCamera().apply {
        setToOrtho(true)
    }
    private val viewport = FitViewport(16f, 10f, camera).also {
        camera.center(it.worldWidth, it.worldHeight)
    }

    private val box2dWorld = createWorld(gravity = earthGravity)

    private val world =
        World {
            inject<Camera>(viewport.camera)
            inject(box2dWorld)
            inject(game)
            componentListener<BodiesListener>()
            inject(PhysicsSystem.Config(step = 1f / 60f))
            system<InputSystem>()
            system<PhysicsSystem>()
            system<RenderSystem>()
            system<PhysicsDebugSystem>()
        }
            .apply {
                level.layers.forEach { gameLayer ->
                    entity { add<LayerComponent> { layer = gameLayer } }
                }
                entity { add<Box2dComponent> { boxes = level.boxes } }
            }

    override fun show() {
        Gdx.input.inputProcessor = world.system<InputSystem>().inputProcessor
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render(delta: Float) {
        camera.update()
        world.update(delta)
    }

    override fun dispose() {
        world.dispose()
        level.dispose()
    }
}
