package io.github.fourlastor.monster.editor.system

import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.One
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.math.Vector3
import io.github.fourlastor.monster.editor.component.ModelInstanceComponent
import io.github.fourlastor.monster.editor.component.PlaneComponent
import io.github.fourlastor.monster.extension.onMapper

@All(ModelInstanceComponent::class)
@One(value = [PlaneComponent::class])
class InputSystem(
    private val camera: Camera,
) : BaseEntitySystem(), InputProcessor {

  private lateinit var instanceMapper: ComponentMapper<ModelInstanceComponent>
  private lateinit var planeMapper: ComponentMapper<PlaneComponent>

  private var plane: Int? = null

  private val processor: CameraInputController =
      object : CameraInputController(camera) {
        private var keycode = -1

        init {
          forwardButton = -2
          rotateButton = Buttons.MIDDLE
        }

        override fun keyDown(keycode: Int): Boolean {
          this.keycode = keycode
          return super.keyDown(keycode)
        }

        override fun keyUp(keycode: Int): Boolean {
          this.keycode = -1
          return super.keyUp(keycode)
        }

        private val controllerTranslation = Vector3()

        override fun scrolled(amountX: Float, amountY: Float): Boolean {
          return when (keycode) {
            Input.Keys.SHIFT_LEFT -> {
              plane?.onMapper(instanceMapper) {
                it.instance?.run {
                  transform.run {
                    setTranslation(getTranslation(controllerTranslation).add(0f, -amountY, 0f))
                  }
                }
              }
              true
            }
            else -> super.scrolled(amountX, amountY)
          }
        }
      }

  override fun inserted(entityId: Int) {
    super.inserted(entityId)
    entityId.onMapper(planeMapper) {
      if (plane != null) {
        throw IllegalStateException(
            "Adding a second plane entity is not permitted, $plane is the current entity")
      }
      plane = entityId
    }
  }

  override fun processSystem() {
    camera.update()
  }

  override fun keyDown(keycode: Int): Boolean = processor.keyDown(keycode)

  override fun keyUp(keycode: Int): Boolean = processor.keyUp(keycode)

  override fun keyTyped(character: Char): Boolean = processor.keyTyped(character)

  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
      processor.touchDown(screenX, screenY, pointer, button)

  override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
      processor.touchUp(screenX, screenY, pointer, button)

  override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean =
      processor.touchDragged(screenX, screenY, pointer)

  override fun mouseMoved(screenX: Int, screenY: Int): Boolean =
      processor.mouseMoved(screenX, screenY)

  override fun scrolled(amountX: Float, amountY: Float): Boolean =
      processor.scrolled(amountX, amountY)
}
