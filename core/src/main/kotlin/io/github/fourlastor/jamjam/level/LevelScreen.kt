package io.github.fourlastor.jamjam.level

import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import io.github.fourlastor.jamjam.JamGame
import io.github.fourlastor.jamjam.extension.component
import io.github.fourlastor.jamjam.extension.create
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.PlayerComponent
import io.github.fourlastor.jamjam.level.component.SpriteComponent
import io.github.fourlastor.jamjam.level.component.StaticBodyComponent
import io.github.fourlastor.jamjam.level.system.CameraFollowPlayerSystem
import io.github.fourlastor.jamjam.level.system.InputSystem
import io.github.fourlastor.jamjam.level.system.PhysicsDebugSystem
import io.github.fourlastor.jamjam.level.system.PhysicsSystem
import io.github.fourlastor.jamjam.level.system.RenderSystem
import io.github.fourlastor.jamjam.level.system.SpriteFollowBodySystem
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLevelDefinition
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.graphics.center

class LevelScreen(
    game: JamGame,
    levelDefinition: LDtkLevelDefinition,
    definitions: Definitions
) : KtxScreen {

    private val converter = LDtkConverter(1f / 16f)
    private val level = converter.convert(levelDefinition, definitions)

    private val camera = OrthographicCamera().apply {
        setToOrtho(true)
    }
    private val viewport = FitViewport(16f, 10f, camera).also {
        camera.center(it.worldWidth, it.worldHeight)
    }

    private val box2dWorld = createWorld(gravity = Vector2(0f, 10f))

    private val debug = true
    private val inputSystem = InputSystem(game = game)

    private val world = WorldConfigurationBuilder().with(
        inputSystem,
        PhysicsSystem(
            config = PhysicsSystem.Config(step = 1f / 60f),
            box2dWorld = box2dWorld,
        ),
        SpriteFollowBodySystem(),
        CameraFollowPlayerSystem(camera = camera),
        RenderSystem(camera = camera),
    )
        .apply {
            if (debug) {
                with(PhysicsDebugSystem(
                    camera = camera,
                    box2dWorld = box2dWorld,
                ))
            }
        }
        .build()
        .let { com.artemis.World(it) }

    init {
        val statics = level.statics
        statics.spriteLayers.forEach { layer ->
            layer.tiles.forEach { tileSprite ->
                world.create {
                    component<SpriteComponent>(it) {
                        priority = layer.layerIndex
                        sprite = tileSprite
                    }
                }
            }
        }
        world.create { component<StaticBodyComponent>(it) { boxes = statics.staticBodies } }

        level.player.also { player ->
            world.create {
                component<PlayerComponent>(it)
                component<SpriteComponent>(it) {
                    priority = player.layerIndex
                    sprite = player.sprite
                }
                component<DynamicBodyComponent>(it) {
                    val sprite = player.sprite
                    box = Rectangle(sprite.boundingRectangle).apply {
                        width *= 0.35f
                        setCenter(sprite.boundingRectangle.getCenter(Vector2()))
                    }
                }
            }
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = inputSystem.inputProcessor
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render(delta: Float) {
        world.setDelta(delta)
        world.process()
    }

    override fun dispose() {
        world.dispose()
        world.dispose()
        box2dWorld.dispose()
        level.dispose()
    }
}
