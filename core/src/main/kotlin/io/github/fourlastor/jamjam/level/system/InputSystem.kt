package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode
import com.badlogic.gdx.math.Rectangle
import io.github.fourlastor.jamjam.extension.State
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.PlayerComponent
import io.github.fourlastor.jamjam.level.component.Render
import io.github.fourlastor.jamjam.level.component.RenderComponent
import ktx.app.KtxInputAdapter

@All(PlayerComponent::class, DynamicBodyComponent::class, RenderComponent::class)
class InputSystem : IteratingSystem() {

    private lateinit var bodies: ComponentMapper<DynamicBodyComponent>
    private lateinit var players: ComponentMapper<PlayerComponent>
    private lateinit var renders: ComponentMapper<RenderComponent>

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
        player.idle = InputState.Idle(renders, players, bodies)
        player.run = InputState.Run(renders, players, bodies)
        player.stateMachine = InputStateMachine(entityId, player.idle).also {
            it.currentState.enter(entityId)
        }
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
    protected val renders: ComponentMapper<RenderComponent>,
    protected val players: ComponentMapper<PlayerComponent>,
    protected val bodies: ComponentMapper<DynamicBodyComponent>,
) : State<Int> {
    open fun keyDown(entity: Int, keycode: Int): Boolean = false
    open fun keyUp(entity: Int, keycode: Int): Boolean = false

    abstract class AnimationState(
        renders: ComponentMapper<RenderComponent>,
        players: ComponentMapper<PlayerComponent>,
        bodies: ComponentMapper<DynamicBodyComponent>,
    ): InputState(renders, players, bodies) {

        protected abstract val animationName:String
        override fun enter(entity: Int) {
            val renderComponent = renders[entity]
            renderComponent.render = Render.AnimationRender(
                animation = Animation(0.15f, renderComponent.atlas.findRegions(animationName), PlayMode.LOOP),
                dimensions = Rectangle(renderComponent.render.dimensions)
            )
        }

        override fun update(entity: Int) {
            super.update(entity)
            renders[entity].render.increaseTime(Gdx.graphics.deltaTime)
        }
    }

    class Run(
        renders: ComponentMapper<RenderComponent>,
        players: ComponentMapper<PlayerComponent>,
        bodies: ComponentMapper<DynamicBodyComponent>
    ): AnimationState(renders, players, bodies) {

        override val animationName: String = "player_run"

        var enterKeyCode: Int? = null

        override fun update(entity: Int) {
            super.update(entity)
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
        renders: ComponentMapper<RenderComponent>,
        players: ComponentMapper<PlayerComponent>,
        bodies: ComponentMapper<DynamicBodyComponent>,
    ): AnimationState(renders, players, bodies) {

        override val animationName: String = "player_stand"

        override fun enter(entity: Int) {
            super.enter(entity)
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
