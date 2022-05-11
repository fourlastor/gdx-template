package io.github.fourlastor.monster.system

import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.PooledComponent
import com.artemis.annotations.One
import com.badlogic.gdx.graphics.g3d.environment.BaseLight
import io.github.fourlastor.monster.extension.onMapper
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneManager

@One(SceneComponent::class, LightComponent::class)
class RenderSystem(
    private val manager: SceneManager,
) : BaseEntitySystem() {

  private lateinit var sceneMapper: ComponentMapper<SceneComponent>
  private lateinit var lightMapper: ComponentMapper<LightComponent>

  override fun processSystem() {
    manager.update(world.delta)
    manager.render()
  }

  override fun dispose() {
    manager.dispose()
  }

  override fun inserted(entityId: Int) {
    entityId.onMapper(sceneMapper) { manager.addScene(it.scene) }
    entityId.onMapper(lightMapper) { manager.environment.add(it.light) }
  }

  override fun removed(entityId: Int) {
    entityId.onMapper(sceneMapper) { manager.removeScene(it.scene) }
    entityId.onMapper(lightMapper) { manager.environment.remove(it.light) }
  }
}

class SceneComponent(var scene: Scene? = null) : PooledComponent() {

  override fun reset() {
    scene = null
  }
}

class LightComponent(var light: BaseLight<*>? = null) : PooledComponent() {
  override fun reset() {
    light = null
  }
}
