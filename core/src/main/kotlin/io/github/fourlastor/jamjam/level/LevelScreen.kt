package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.github.quillraven.fleks.World
import io.github.fourlastor.jamjam.JamGame
import io.github.fourlastor.jamjam.level.system.BodiesListener
import io.github.fourlastor.jamjam.level.system.Box2dComponent
import io.github.fourlastor.jamjam.level.system.InputSystem
import io.github.fourlastor.jamjam.level.system.PhysicsDebugSystem
import io.github.fourlastor.jamjam.level.system.PhysicsSystem
import io.github.fourlastor.jamjam.level.system.RenderSystem
import io.github.fourlastor.jamjam.level.system.SpriteComponent
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

    private val box2dWorld = createWorld(gravity = earthGravity)

    private val debug = true

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
            if (debug) {
                system<PhysicsDebugSystem>()
            }
        }
            .apply {
                level.statics.spriteLayers.forEach { layer ->
                    layer.tiles.forEach {  tileSprite ->
                        entity {
                            add<SpriteComponent> {
                                priority = layer.layerIndex
                                sprite = tileSprite
                            }
                        }
                    }
                }

                entity { add<Box2dComponent> { boxes = level.statics.staticBodies } }
                level.player.also { player ->
                    entity {
                        add<SpriteComponent> {
                            priority = player.layerIndex
                            sprite = player.sprite
                        }
                        add<Box2dComponent> {
                            val sprite = player.sprite
                            boxes = listOf(
                                Rectangle(sprite.boundingRectangle).apply {
                                    width *= 0.35f
                                    setCenter(sprite.boundingRectangle.getCenter(Vector2()))
                                }
                            )
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
