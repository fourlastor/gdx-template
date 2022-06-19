package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
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

class BodiesListener(
    private val box2dWorld: World,
) : ComponentListener<Box2dComponent> {

    override fun onComponentAdded(entity: Entity, component: Box2dComponent) {
        component.body = box2dWorld.body {
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

    override fun onComponentRemoved(entity: Entity, component: Box2dComponent) = Unit
}

class Box2dComponent {
    lateinit var boxes: List<Rectangle>
    lateinit var body: Body
}
