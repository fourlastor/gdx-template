package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import io.github.fourlastor.jamjam.extension.State
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.PlayerComponent
import ktx.app.KtxInputAdapter

@All(PlayerComponent::class, DynamicBodyComponent::class)
class InputSystem : IteratingSystem() {

    private lateinit var bodies: ComponentMapper<DynamicBodyComponent>
    private lateinit var players: ComponentMapper<PlayerComponent>

    val inputProcessor: InputProcessor = object : KtxInputAdapter {
        override fun keyDown(keycode: Int): Boolean {
            return forward { keyDown(keycode) }
        }

        override fun keyUp(keycode: Int): Boolean {
            return forward { keyUp(keycode) }
        }

        private fun forward(action: InputStateMachine.() -> Boolean): Boolean {
            val data = entityIds.data
            for (i in 0 until entityIds.size()) {
                val player = players[data[i]]
                if (player.stateMachine.action()) {
                    return true
                }
            }
            return false
        }
    }


    override fun process(entityId: Int) {
        players[entityId].stateMachine.update()
    }

    override fun inserted(entityId: Int) {
        val player = players[entityId]
        player.idle = InputState.Idle(players, bodies)
        player.run = InputState.Run(players, bodies)
        player.stateMachine = InputStateMachine(entityId, player.idle)
    }
}

class InputStateMachine(
    entity: Int,
    initialState: InputState,
) : DefaultStateMachine<Int, InputState>(entity, initialState), KtxInputAdapter {

    override fun keyDown(keycode: Int) = onState { keyDown(owner, keycode) }

    override fun keyUp(keycode: Int) = onState { keyUp(owner, keycode) }

    private inline fun onState(action: InputState.() -> Boolean): Boolean =
        currentState?.run(action) == true || globalState?.run(action) == true
}

sealed class InputState(
    protected val players: ComponentMapper<PlayerComponent>,
    protected val bodies: ComponentMapper<DynamicBodyComponent>,
) : State<Int> {
    open fun keyDown(entity: Int, keycode: Int): Boolean = false
    open fun keyUp(entity: Int, keycode: Int): Boolean = false

    class Run(
        players: ComponentMapper<PlayerComponent>,
        bodies: ComponentMapper<DynamicBodyComponent>
    ): InputState(players, bodies) {

        var enterKeyCode: Int? = null

        override fun update(entity: Int) {
            val velocityX = when (enterKeyCode) {
                Input.Keys.A -> -3f
                Input.Keys.D -> 3f
                else -> 0f
            }
            bodies[entity].body.setLinearVelocity(velocityX, 0f)
        }

        override fun keyDown(entity: Int, keycode: Int): Boolean {
            return when(keycode) {
                Input.Keys.A, Input.Keys.D -> {
                    enterKeyCode = keycode
                    true
                }
                else -> super.keyDown(entity, keycode)
            }
        }

        override fun keyUp(entity: Int, keycode: Int): Boolean {
            return if (keycode == enterKeyCode) {
                val player = players[entity]
                player.stateMachine.changeState(player.idle)
                true
            } else {
                super.keyUp(entity, keycode)
            }
        }
    }
    class Idle(
        players: ComponentMapper<PlayerComponent>,
        bodies: ComponentMapper<DynamicBodyComponent>
    ): InputState(players, bodies) {
        override fun enter(entity: Int) {
            bodies[entity].body.setLinearVelocity(0f, 0f)
        }

        override fun keyDown(entity: Int, keycode: Int): Boolean {
            return when (keycode) {
                Input.Keys.A, Input.Keys.D -> {
                    val player = players[entity]
                    player.stateMachine.changeState(player.run.apply {
                        enterKeyCode = keycode
                    })
                    true
                }
                else -> super.keyDown(entity, keycode)
            }
        }
    }
}
