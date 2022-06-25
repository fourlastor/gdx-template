package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import io.github.fourlastor.jamjam.AssetFactory
import io.github.fourlastor.jamjam.extension.State
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.PlayerComponent
import io.github.fourlastor.jamjam.level.component.Render
import io.github.fourlastor.jamjam.level.component.RenderComponent
import ktx.app.KtxInputAdapter

@All(PlayerComponent::class, DynamicBodyComponent::class, RenderComponent::class)
class InputSystem(
    private val factory: AssetFactory,
    private val config: Config,
) : IteratingSystem() {

    fun updateConfig(update: Config.() -> Unit) {
        config.apply(update)
    }

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
        val dependencies = InputState.Dependencies(
            renders,
            players,
            bodies,
            factory,
        )
        player.onGround = InputState.OnGround(dependencies, config)
        player.stateMachine = InputStateMachine(entityId, player.onGround).also {
            it.currentState.enter(entityId)
        }
    }
}

data class Config(
    var speed: Float,
)

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
    private val dependencies: Dependencies,
) : State<Int> {
    class Dependencies(
        val renders: ComponentMapper<RenderComponent>,
        val players: ComponentMapper<PlayerComponent>,
        val bodies: ComponentMapper<DynamicBodyComponent>,
        val factory: AssetFactory,
    )

    protected val factory: AssetFactory
        get() = dependencies.factory

    protected val Int.render: RenderComponent
        get() = dependencies.renders[this]
    protected val Int.body: DynamicBodyComponent
        get() = dependencies.bodies[this]
    protected val Int.player: PlayerComponent
        get() = dependencies.players[this]

    protected fun updateAnimation(
        entity: Int,
        animation: Animation<Sprite>,
    ) {
        entity.render.render = Render.AnimationRender(
            animation,
            entity.render.render.dimensions,
        )
    }

    open fun keyDown(entity: Int, keycode: Int): Boolean = false
    open fun keyUp(entity: Int, keycode: Int): Boolean = false

    class OnGround(
        dependencies: Dependencies,
        private val config: Config,
    ): InputState(dependencies) {

        private var speed: Float = 0f
        private var lastKey = -1
        override fun enter(entity: Int) {
            updateAnimation(entity, factory.characterStanding())
        }

        override fun keyDown(entity: Int, keycode: Int): Boolean {
            lastKey = keycode
            return when (keycode) {
                Keys.A -> {
                    speed = -config.speed
                    updateAnimation(entity, factory.characterRunning())
                    entity.render.flipX = true
                    true
                }
                Keys.D -> {
                    speed = config.speed
                    updateAnimation(entity, factory.characterRunning())
                    entity.render.flipX = false
                    true
                }
                else -> false
            }
        }

        override fun keyUp(entity: Int, keycode: Int): Boolean {
            if (lastKey != keycode) {
                // TODO this is gonna break
                return false
            }
            return when (keycode) {
                Keys.A, Keys.D -> {
                    speed = 0f
                    updateAnimation(entity, factory.characterStanding())
                    true
                }
                else -> false
            }
        }

        override fun update(entity: Int) {
            val body = entity.body.body
            body.setLinearVelocity(speed, body.linearVelocity.y)
            entity.render.render.increaseTime(Gdx.graphics.deltaTime)
        }
    }
}
