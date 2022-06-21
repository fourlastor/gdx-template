package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.fleks.World
import io.github.fourlastor.jamjam.JamGame
import io.github.fourlastor.jamjam.level.system.CameraFollowPlayerSystem
import io.github.fourlastor.jamjam.level.system.InputSystem
import io.github.fourlastor.jamjam.level.system.KinematicBodyComponent
import io.github.fourlastor.jamjam.level.system.KinematicBodyListener
import io.github.fourlastor.jamjam.level.system.PhysicsDebugSystem
import io.github.fourlastor.jamjam.level.system.PhysicsSystem
import io.github.fourlastor.jamjam.level.system.PlayerComponent
import io.github.fourlastor.jamjam.level.system.RenderSystem
import io.github.fourlastor.jamjam.level.system.SpriteComponent
import io.github.fourlastor.jamjam.level.system.SpriteFollowBodySystem
import io.github.fourlastor.jamjam.level.system.StaticBodyComponent
import io.github.fourlastor.jamjam.level.system.StaticBodyListener
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLevelDefinition
import ktx.app.KtxScreen
import ktx.box2d.createWorld
import ktx.box2d.earthGravity
import ktx.graphics.center

class LevelScreen(
    private val game: JamGame,
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

    private val world =
        World {
            inject<Camera>(viewport.camera)
            inject(box2dWorld)
            inject(game)
            componentListener<StaticBodyListener>()
            componentListener<KinematicBodyListener>()
            inject(PhysicsSystem.Config(step = 1f / 60f))
            system<InputSystem>()
            system<PhysicsSystem>()
            system<SpriteFollowBodySystem>()
            system<CameraFollowPlayerSystem>()
            system<RenderSystem>()
            if (debug) {
                system<PhysicsDebugSystem>()
            }
        }
            .apply {
                val statics = level.statics
                statics.spriteLayers.forEach { layer ->
                    layer.tiles.forEach { tileSprite ->
                        entity {
                            add<SpriteComponent> {
                                priority = layer.layerIndex
                                sprite = tileSprite
                            }
                        }
                    }
                }

                statics.staticBodies.forEach { boxBody -> entity { add<StaticBodyComponent> { box = boxBody } } }
                level.player.also { player ->
                    entity {
                        add<PlayerComponent>()
                        add<SpriteComponent> {
                            priority = player.layerIndex
                            sprite = player.sprite
                        }
                        add<KinematicBodyComponent> {
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
        Gdx.input.inputProcessor = world.system<InputSystem>().inputProcessor
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, false)
    }

    override fun render(delta: Float) {
        world.update(delta)
    }

    override fun dispose() {
        world.dispose()
        box2dWorld.dispose()
        level.dispose()
    }
}
