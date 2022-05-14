package io.github.fourlastor.monster.input

import com.badlogic.gdx.ai.fsm.DefaultStateMachine
import io.github.fourlastor.monster.editor.system.InputSystem
import io.github.fourlastor.monster.extension.State
import ktx.app.KtxInputAdapter

class InputStateMachine(
    entity: InputSystem,
    initialState: InputState,
) : DefaultStateMachine<InputSystem, InputState>(entity, initialState), KtxInputAdapter {

  override fun keyDown(keycode: Int) = onState { keyDown(owner, keycode) }

  override fun keyUp(keycode: Int) = onState { keyUp(owner, keycode) }

  override fun scrolled(amountX: Float, amountY: Float) = onState {
    scrolled(owner, amountX, amountY)
  }

  private inline fun onState(action: InputState.() -> Boolean): Boolean =
      currentState?.run(action) == true || globalState?.run(action) == true
}

interface InputState : State<InputSystem> {
  fun keyDown(entity: InputSystem, keycode: Int): Boolean = false
  fun keyUp(entity: InputSystem, keycode: Int): Boolean = false
  fun scrolled(entity: InputSystem, amountX: Float, amountY: Float): Boolean = false
}
