package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import ktx.box2d.body
import ktx.box2d.box

class PhysicsSystem(
    private val config: Config,
    private val box2dWorld: World,
) : IntervalSystem(interval = Fixed(config.step)) {

    override fun onTick() {
        box2dWorld.step(config.step, 6, 2)
    }

    data class Config(
        val step: Float,
    )
}

abstract class BodyListener<T: BodyComponent>(
    private val box2dWorld: World,
): ComponentListener<T> {

    abstract val bodyType: BodyType

    override fun onComponentAdded(entity: Entity, component: T) {
        component.body = box2dWorld.body(type = bodyType) {
            component.boxes.forEach { box ->
                box(
                    width = box.width,
                    height = box.height,
                    position = Vector2(
                        box.x + box.width / 2,
                        box.y + box.height / 2
                    )
                )
            }
        }
    }

    override fun onComponentRemoved(entity: Entity, component: T) = Unit
}

class StaticBodyListener(box2dWorld: World) : BodyListener<StaticBodyComponent>(box2dWorld), ComponentListener<StaticBodyComponent> {
    override val bodyType: BodyType
        get() = BodyType.StaticBody
}

class KinematicBodyListener(box2dWorld: World) : BodyListener<KinematicBodyComponent>(box2dWorld), ComponentListener<KinematicBodyComponent> {
    override val bodyType: BodyType
        get() = BodyType.KinematicBody
}

interface BodyComponent {
    var body: Body
    var boxes: List<Rectangle>
}

class StaticBodyComponent: BodyComponent {
    override lateinit var boxes: List<Rectangle>
    override lateinit var body: Body
}

class KinematicBodyComponent: BodyComponent {
    override lateinit var boxes: List<Rectangle>
    override lateinit var body: Body
}
