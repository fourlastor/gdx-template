package io.github.fourlastor.monster.editor.system

import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.ModelBatch
import io.github.fourlastor.monster.editor.component.ModelInstanceComponent
import io.github.fourlastor.monster.extension.onMapper

@All(ModelInstanceComponent::class)
class RenderSystem(
    private val camera: Camera
) : BaseEntitySystem() {

    private lateinit var sceneMapper: ComponentMapper<ModelInstanceComponent>

    private val batch = ModelBatch()

    override fun processSystem() {
        batch.begin(camera)
        for (i in 0 until entityIds.size()) {
            entityIds.get(i).onMapper(sceneMapper) {
                val instance = it.instance ?: return@onMapper
                batch.render(instance)
            }
        }
        batch.end()
    }

    override fun dispose() {
        batch.dispose()
    }

}


