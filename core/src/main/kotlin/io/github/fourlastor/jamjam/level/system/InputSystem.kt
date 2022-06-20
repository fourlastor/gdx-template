package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.physics.box2d.Body
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import io.github.fourlastor.jamjam.JamGame
import ktx.app.KtxInputAdapter

@AllOf([PlayerComponent::class, KinematicBodyComponent::class])
class InputSystem(
    private val game: JamGame,
    private val bodies: ComponentMapper<KinematicBodyComponent>,
) : IteratingSystem() {

    private var state = Movement.STANDING

    val inputProcessor: InputProcessor = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            return when (keycode) {
                Input.Keys.A -> {
                    state = Movement.LEFT
                    true
                }

                Input.Keys.D -> {
                    state = Movement.RIGHT
                    true
                }

                Input.Keys.ESCAPE -> {
                    game.goToMenu()
                    true
                }

                else -> false
            }
        }

        override fun keyUp(keycode: Int): Boolean {
            return if (keycode == state.key) {
                state = Movement.STANDING
                true
            } else {
                false
            }
        }
    }

    override fun onTickEntity(entity: Entity) {
        state.move(bodies[entity].body, deltaTime * 10)
    }

    private enum class Movement(val key: Int?) {
        STANDING(null) {
            override fun move(body: Body, factor: Float) {
                body.setLinearVelocity(0f, 0f)
            }
        },
        LEFT(Input.Keys.A) {
            override fun move(body: Body, factor: Float) {
                body.setLinearVelocity(-3f, 0f)
            }
        },
        RIGHT(Input.Keys.D) {
            override fun move(body: Body, factor: Float) {
                body.setLinearVelocity(3f, 0f)
            }
        };

        abstract fun move(body: Body, factor: Float)
    }
}

class PlayerComponent
