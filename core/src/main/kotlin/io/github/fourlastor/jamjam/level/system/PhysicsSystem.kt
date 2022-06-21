package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.ComponentListener
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.Fixed
import com.github.quillraven.fleks.IntervalSystem
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.chain
import ktx.math.vec2

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

class StaticBodyListener(private val box2dWorld: World): ComponentListener<StaticBodyComponent> {

    override fun onComponentAdded(entity: Entity, component: StaticBodyComponent) {
        component.body = box2dWorld.body(type = BodyType.StaticBody) {
            component.boxes.forEach { box ->
                chain(
                    vec2(box.x, box.y),
                    vec2(box.x + box.width, box.y),
                    vec2(box.x + box.width, box.y + box.height),
                    vec2(box.x, box.y + box.height),
                )

            }
        }
    }

    override fun onComponentRemoved(entity: Entity, component: StaticBodyComponent) = Unit
}

class DynamicBodyListener(
    private val box2dWorld: World,
) : ComponentListener<DynamicBodyComponent> {

    override fun onComponentAdded(entity: Entity, component: DynamicBodyComponent) {
        component.body = box2dWorld.body(type = BodyType.DynamicBody) {
            val box = component.box
            position.apply {
                x = box.x + box.width / 2
                y = box.y + box.height / 2
            }
            box(
                width = box.width,
                height = box.height,
            )
        }
    }

    override fun onComponentRemoved(entity: Entity, component: DynamicBodyComponent) = Unit
}

class StaticBodyComponent {
    lateinit var boxes: List<Rectangle>
    lateinit var body: Body
}

class DynamicBodyComponent {
    lateinit var box: Rectangle
    lateinit var body: Body
}
