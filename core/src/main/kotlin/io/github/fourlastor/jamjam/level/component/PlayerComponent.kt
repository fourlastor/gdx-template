package io.github.fourlastor.jamjam.level.component

import com.artemis.Component
import io.github.fourlastor.jamjam.level.system.InputState
import io.github.fourlastor.jamjam.level.system.InputStateMachine

class PlayerComponent: Component() {
    lateinit var stateMachine: InputStateMachine
    lateinit var run: InputState.Run
    lateinit var idle: InputState.Idle
}
