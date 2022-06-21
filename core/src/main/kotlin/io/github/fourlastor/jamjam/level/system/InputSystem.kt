package io.github.fourlastor.jamjam.level.system

import com.artemis.Component
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.physics.box2d.Body
import io.github.fourlastor.jamjam.JamGame
import ktx.app.KtxInputAdapter

@All(PlayerComponent::class, DynamicBodyComponent::class)
class InputSystem(
    private val game: JamGame,
) : SingleEntitySystem() {

    private lateinit var bodies: ComponentMapper<DynamicBodyComponent>

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

    override fun processSystem() {
        val entity = entity ?: return
        state.move(bodies[entity].body)
    }

    private enum class Movement(val key: Int?) {
        STANDING(null) {
            override fun move(body: Body) {
                body.setLinearVelocity(0f, 0f)
            }
        },
        LEFT(Input.Keys.A) {
            override fun move(body: Body) {
                body.setLinearVelocity(-3f, 0f)
            }
        },
        RIGHT(Input.Keys.D) {
            override fun move(body: Body) {
                body.setLinearVelocity(3f, 0f)
            }
        };

        abstract fun move(body: Body)
    }
}

class PlayerComponent: Component()
