package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.collection.compareEntity
import io.github.fourlastor.jamjam.level.WorldLevel
import ktx.app.clearScreen
import ktx.graphics.use

@AllOf([LayerComponent::class])
class RenderSystem(
    private val layers: ComponentMapper<LayerComponent>,
    private val camera: OrthographicCamera,
) :
    IteratingSystem(
        compareEntity { entity, entity2 ->
            layers[entity].order.compareTo(layers[entity2].order)
        }) {

    private val batch = SpriteBatch()

    override fun onTick() {
        clearScreen(0.3f, 0.3f, 0.3f)
        batch.use(camera) { super.onTick() }
    }

    override fun onTickEntity(entity: Entity) {
        layers[entity].layer.render(batch)
    }

    override fun onDispose() {
        batch.dispose()
    }
}

class LayerComponent {
    lateinit var layer: WorldLevel.Layer

    val order: Int
        get() = layer.order
}
