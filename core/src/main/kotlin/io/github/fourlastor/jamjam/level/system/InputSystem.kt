package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Camera
import com.github.quillraven.fleks.IntervalSystem
import io.github.fourlastor.jamjam.JamGame
import ktx.app.KtxInputAdapter
import ktx.graphics.update

class InputSystem(
    private val camera: Camera,
    private val game: JamGame,
) : IntervalSystem() {

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

    override fun onTick() {
        state.move(camera, deltaTime * 10)
    }

    private enum class Movement(val key: Int?) {
        STANDING(null) {
            override fun move(camera: Camera, factor: Float) = Unit
        },
        LEFT(Input.Keys.A) {
            override fun move(camera: Camera, factor: Float) {
                camera.update { translate(-factor, 0f, 0f) }
            }
        },
        RIGHT(Input.Keys.D) {
            override fun move(camera: Camera, factor: Float) {
                camera.update { translate(factor, 0f, 0f) }
            }
        };

        abstract fun move(camera: Camera, factor: Float)
    }
}
