package io.github.fourlastor.jamjam.level.system

import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import io.github.fourlastor.jamjam.level.component.SpriteComponent
import ktx.graphics.use

@All(SpriteComponent::class)
class RenderSystem(
    private val camera: Camera,
) : BaseEntitySystem() {

    private lateinit var sprites: ComponentMapper<SpriteComponent>

    private val batch = SpriteBatch()

    override fun processSystem() {
        batch.use(camera) { batch ->
            val actives = subscription.entities
            val ids = actives.data
            for (i in 0 until actives.size()) {
                process(batch, ids[i])
            }
        }
    }

    private fun process(batch: SpriteBatch, entityId: Int) {
        sprites[entityId].sprite.draw(batch)
    }

    override fun dispose() {
        batch.dispose()
    }
}
