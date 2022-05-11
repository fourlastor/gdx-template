package io.github.fourlastor.monster

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.IntIntMap
import io.github.fourlastor.monster.system.LightComponent
import io.github.fourlastor.monster.system.RenderSystem
import io.github.fourlastor.monster.system.SceneComponent
import ktx.app.KtxScreen
import net.mgsx.gltf.loaders.glb.GLBLoader
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx
import net.mgsx.gltf.scene3d.scene.Scene
import net.mgsx.gltf.scene3d.scene.SceneAsset
import net.mgsx.gltf.scene3d.scene.SceneManager

class SystemTestScreen : KtxScreen {

  private val sceneAsset: SceneAsset =
      GLBLoader().load(Gdx.files.internal("../assets/character.glb"))

  private val sceneManager: SceneManager = SceneManager()
  private val world =
      WorldConfigurationBuilder().with(RenderSystem(sceneManager)).build().let { World(it) }

  private val camera: PerspectiveCamera =
      PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        translate(Vector3(2f, 2f, -3f))
        lookAt(Vector3.Zero)
      }

  private val cameraController =
      object : InputAdapter() {
        private val keys = IntIntMap()

        override fun keyDown(keycode: Int): Boolean {
          keys.put(keycode, keycode)
          return true
        }

        override fun keyUp(keycode: Int): Boolean {
          keys.remove(keycode, 0)
          return true
        }

        fun update(delta: Float) {
          val rotation = delta * 100
          when {
            keys.containsKey(Input.Keys.A) -> {
              camera.rotateAround(Vector3.Y, Vector3.Y, rotation)
              camera.lookAt(Vector3.Zero)
              camera.update(true)
            }
            keys.containsKey(Input.Keys.D) -> {
              camera.rotateAround(Vector3.Y, Vector3.Y, -rotation)
              camera.lookAt(Vector3.Zero)
              camera.update(true)
            }
          }
        }
      }

  init {
    world.create().also {
      world.edit(it).create(SceneComponent::class.java).apply {
        scene =
            Scene(sceneAsset.scene).apply { animationController.animate("idle", -1, 1f, null, 0f) }
      }
    }
    world.create().also {
      world.edit(it).create(LightComponent::class.java).apply {
        light =
            DirectionalLightEx().apply {
              direction.set(1f, -3f, 1f).nor()
              color.set(Color.WHITE)
            }
      }
    }
    sceneManager.setCamera(camera)
    sceneManager.setAmbientLight(0.2f)
  }

  override fun show() {
    Gdx.input.inputProcessor = cameraController
  }

  override fun resize(width: Int, height: Int) {
    sceneManager.updateViewport(width.toFloat(), height.toFloat())
  }

  override fun render(delta: Float) {
    world.setDelta(delta)
    world.process()
    cameraController.update(delta)
  }

  override fun dispose() {
    sceneAsset.dispose()
    world.dispose()
  }
}
