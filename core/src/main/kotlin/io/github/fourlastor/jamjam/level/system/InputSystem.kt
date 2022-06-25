package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import com.badlogic.gdx.ai.msg.MessageManager
import com.badlogic.gdx.ai.msg.Telegram
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import io.github.fourlastor.jamjam.AssetFactory
import io.github.fourlastor.jamjam.extension.State
import io.github.fourlastor.jamjam.level.component.PlayerBodyComponent
import io.github.fourlastor.jamjam.level.component.PlayerComponent
import io.github.fourlastor.jamjam.level.component.Render
import io.github.fourlastor.jamjam.level.component.RenderComponent
import ktx.app.KtxInputAdapter

@All(PlayerComponent::class, PlayerBodyComponent::class, RenderComponent::class)
class InputSystem(
    private val factory: AssetFactory,
    private val config: Config,
    box2dWorld: World,
) : IteratingSystem() {

    private val messageManager = MessageManager.getInstance()

    init {
        box2dWorld.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                if (contact.fixtureA.userData == "foot" || contact.fixtureB.userData == "foot") {
                    messageManager.dispatchMessage(Message.PLAYER_ON_GROUND.ordinal)
                }
            }

            override fun endContact(contact: Contact) {
                if (contact.fixtureA.userData == "foot" || contact.fixtureB.userData == "foot") {
                    messageManager.dispatchMessage(Message.PLAYER_OFF_GROUND.ordinal)
                }
            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) = Unit

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) = Unit

        })
    }

    fun updateConfig(update: Config.() -> Unit) {
        config.apply(update)
    }

    private lateinit var bodies: ComponentMapper<PlayerBodyComponent>
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
        player.onGround = OnGround(dependencies, config)
        player.jumping = Jumping(dependencies, config)
        player.falling = Falling(dependencies, config)
        player.stateMachine = InputStateMachine(entityId, player.onGround).also {
            it.currentState.enter(entityId)
        }.also {
            Message.values().forEach { message ->
                messageManager.addListener(it, message.ordinal)
            }

        }
    }
}

private enum class Message {
    PLAYER_ON_GROUND,
    PLAYER_OFF_GROUND,
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
        val bodies: ComponentMapper<PlayerBodyComponent>,
        val factory: AssetFactory,
    )

    protected val factory: AssetFactory
        get() = dependencies.factory

    protected val Int.render: RenderComponent
        get() = dependencies.renders[this]
    protected val Int.body: PlayerBodyComponent
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

}

private class OnGround(
    dependencies: Dependencies,
    config: Config,
) : LateralMovement(dependencies, config) {

    private var state: State = State.STANDING

    override fun keyDown(entity: Int, keycode: Int): Boolean {
        return when (keycode) {
            Keys.SPACE -> {
                entity.player.stateMachine.changeState(entity.player.jumping)
                true
            }

            else -> super.keyDown(entity, keycode)
        }
    }

    override fun enter(entity: Int) {
        super.enter(entity)
        if (entity.body.body.linearVelocity.x == 0f) {
            entity.enterStanding()
        } else {
            entity.enterRunning()
        }
    }

    override fun update(entity: Int) {
        super.update(entity)
        when {
            entity.body.body.linearVelocity.x == 0f && state != State.STANDING  -> {
                entity.enterStanding()
            }
            entity.body.body.linearVelocity.x != 0f && state != State.RUNNING  -> {
                entity.enterRunning()
            }
        }
    }

    private fun Int.enterRunning() {
        state = State.RUNNING
        updateAnimation(this, factory.characterRunning())
    }

    private fun Int.enterStanding() {
        state = State.STANDING
        updateAnimation(this, factory.characterStanding())
    }

    private enum class State {RUNNING, STANDING }
}

private class Jumping(
    dependencies: Dependencies,
    config: Config,
) : LateralMovement(dependencies, config) {

    private var initialPosition: Float = -0f
    override fun enter(entity: Int) {
        super.enter(entity)
        initialPosition = entity.body.body.position.y
        updateAnimation(entity, factory.characterStanding())
    }

    override fun update(entity: Int) {
        super.update(entity)
        val body = entity.body.body
        val position = body.position.y

        if (position - initialPosition <= -3f) {
            entity.player.stateMachine.changeState(entity.player.falling)
            return
        }

        body.setLinearVelocity(body.linearVelocity.x, -4f)
    }

    override fun keyUp(entity: Int, keycode: Int): Boolean {
        if (keycode == Keys.SPACE) {
            entity.player.stateMachine.changeState(entity.player.falling)
            return true
        }
        return super.keyUp(entity, keycode)
    }
}

private class Falling(
    dependencies: Dependencies,
    config: Config,
) : LateralMovement(dependencies, config) {
    override fun enter(entity: Int) {
        val body = entity.body.body
        body.setLinearVelocity(body.linearVelocity.x, 0f)
    }

    override fun onMessage(entity: Int, telegram: Telegram): Boolean {
        if (telegram.message == Message.PLAYER_ON_GROUND.ordinal) {
            entity.player.stateMachine.changeState(entity.player.onGround)
            return true
        }
        return false
    }
}

abstract class LateralMovement(
    dependencies: Dependencies,
    private val config: Config,
) : InputState(dependencies) {

    override fun update(entity: Int) {
        val body = entity.body.body

        val velocityX = when {
            Gdx.input.isKeyPressed(Keys.A) -> -config.speed
            Gdx.input.isKeyPressed(Keys.D) -> config.speed
            else -> 0f
        }
        entity.render.flipX = when {
            velocityX < 0f -> {
                true
            }

            velocityX > 0f -> {
                false
            }

            else -> {
                entity.render.flipX
            }
        }
        body.setLinearVelocity(velocityX, body.linearVelocity.y)
        entity.render.render.increaseTime(Gdx.graphics.deltaTime)
    }
}
