package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.graphics.g2d.Sprite
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([DynamicBodyComponent::class, SpriteComponent::class])
class SpriteFollowBodySystem(
    private val bodies: ComponentMapper<DynamicBodyComponent>,
    private val sprites: ComponentMapper<SpriteComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val center = bodies[entity].body.position
        val sprite = sprites[entity].sprite
        sprite.setCenterScaled(center.x, center.y)
    }

    /** Variant of [Sprite.setCenter] which adapts the width to the scale. */
    private fun Sprite.setCenterScaled(x: Float, y: Float) =
        setPosition(x - width * scaleX / 2, y - height * scaleY / 2)
}
