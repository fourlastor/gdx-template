package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.One
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.World
import io.github.fourlastor.jamjam.extension.BaseEntityIntervalSystem
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.StaticBodyComponent
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.chain
import ktx.math.vec2

@One(StaticBodyComponent::class, DynamicBodyComponent::class)
class PhysicsSystem(
    private val config: Config,
    private val box2dWorld: World,
) : BaseEntityIntervalSystem(config.step) {

    private lateinit var statics: ComponentMapper<StaticBodyComponent>
    private lateinit var dynamics: ComponentMapper<DynamicBodyComponent>

    override fun processSystem() {
        box2dWorld.step(config.step, 6, 2)
    }

    override fun inserted(entityId: Int) {
        when {
            statics.has(entityId) -> {
                val component = statics[entityId]
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

            dynamics.has(entityId) -> {
                val component = dynamics[entityId]
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
        }
    }

    override fun removed(entityId: Int) {
        super.removed(entityId)
        // TODO destroy body
    }

    data class Config(
        val step: Float,
    )
}
