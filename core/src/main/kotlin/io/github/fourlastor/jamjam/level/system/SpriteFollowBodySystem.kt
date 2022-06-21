package io.github.fourlastor.jamjam.level.system

import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.systems.IteratingSystem
import com.badlogic.gdx.graphics.g2d.Sprite
import io.github.fourlastor.jamjam.level.component.DynamicBodyComponent
import io.github.fourlastor.jamjam.level.component.SpriteComponent

@All(DynamicBodyComponent::class, SpriteComponent::class)
class SpriteFollowBodySystem : IteratingSystem() {

    private lateinit var bodies: ComponentMapper<DynamicBodyComponent>
    private lateinit var sprites: ComponentMapper<SpriteComponent>

    override fun process(entityId: Int) {
        val center = bodies[entityId].body.position
        val sprite = sprites[entityId].sprite
        sprite.setCenterScaled(center.x, center.y)
    }

    /** Variant of [Sprite.setCenter] which adapts the width to the scale. */
    private fun Sprite.setCenterScaled(x: Float, y: Float) =
        setPosition(x - width * scaleX / 2, y - height * scaleY / 2)
}
