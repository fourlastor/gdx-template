package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.collection.compareEntity
import ktx.graphics.use

@AllOf([SpriteComponent::class])
class RenderSystem(
    private val sprites: ComponentMapper<SpriteComponent>,
    private val camera: Camera,
) :
    IteratingSystem(
        compareEntity { entity, entity2 ->
            sprites[entity].priority.compareTo(sprites[entity2].priority)
        }) {

    private val batch = SpriteBatch()

    override fun onTick() {
        batch.use(camera) { super.onTick() }
    }

    override fun onTickEntity(entity: Entity) {
        sprites[entity].sprite.draw(batch)
    }

    override fun onDispose() {
        batch.dispose()
    }
}

class SpriteComponent {
    lateinit var sprite: Sprite
    var priority: Int = -1
}
