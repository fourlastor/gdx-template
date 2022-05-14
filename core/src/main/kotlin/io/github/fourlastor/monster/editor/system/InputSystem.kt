package io.github.fourlastor.monster.editor.system

import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.One
import com.badlogic.gdx.Input
import com.badlogic.gdx.Input.Buttons
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController
import com.badlogic.gdx.math.Vector3
import io.github.fourlastor.monster.editor.component.ModelInstanceComponent
import io.github.fourlastor.monster.editor.component.PlaneComponent
import io.github.fourlastor.monster.extension.onMapper
import io.github.fourlastor.monster.input.InputState
import io.github.fourlastor.monster.input.InputStateMachine

@All(ModelInstanceComponent::class)
@One(value = [PlaneComponent::class])
class InputSystem(
    private val camera: Camera,
) : BaseEntitySystem() {

  private lateinit var instanceMapper: ComponentMapper<ModelInstanceComponent>
  private lateinit var planeMapper: ComponentMapper<PlaneComponent>

  private var plane: Int? = null

  private val stateMachine = InputStateMachine(this, State.IDLE)

  private enum class State : InputState {
    IDLE {
      override fun keyDown(entity: InputSystem, keycode: Int) =
          when (keycode) {
            Input.Keys.SHIFT_LEFT -> {
              entity.stateMachine.changeState(MOVING_PLANE)
              true
            }
            else -> false
          }
    },
    MOVING_PLANE {
      override fun scrolled(entity: InputSystem, amountX: Float, amountY: Float): Boolean =
          entity.movePlane(amountY).let { true }

      override fun keyUp(entity: InputSystem, keycode: Int): Boolean =
          keycode == Input.Keys.SHIFT_LEFT && entity.stateMachine.revertToPreviousState()
    }
  }

  val processor: InputProcessor =
      InputMultiplexer(
          stateMachine,
          CameraInputController(camera).apply {
            forwardButton = -2
            rotateButton = Buttons.MIDDLE
          })

  private val planeTranslation = Vector3()

  private fun movePlane(amountY: Float) {
    plane?.onMapper(instanceMapper) {
      it.instance?.run {
        transform.run { setTranslation(getTranslation(planeTranslation).add(0f, -amountY, 0f)) }
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
    stateMachine.update()
    camera.update()
  }
}
