package io.github.fourlastor.monster.editor

import com.artemis.World
import com.artemis.WorldConfigurationBuilder
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import io.github.fourlastor.monster.editor.component.ModelInstanceComponent
import io.github.fourlastor.monster.editor.component.PlaneComponent
import io.github.fourlastor.monster.editor.system.InputSystem
import io.github.fourlastor.monster.editor.system.RenderSystem
import io.github.fourlastor.monster.extension.create
import ktx.app.KtxScreen
import ktx.app.clearScreen


class EditorScreen : KtxScreen {

    private val factory = ModelFactory()

    private val camera =
        PerspectiveCamera(60f, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            near = 0.1f
            translate(-5f, 0f, -10f)
            lookAt(Vector3.Zero)
        }

    private val inputSystem = InputSystem(
        camera,
    )

    private val world = WorldConfigurationBuilder()
        .with(inputSystem)
        .with(RenderSystem(camera))
        .build()
        .let { World(it) }

    init {
        world.create {
            add(ModelInstanceComponent(factory.createPlane()))
            add(PlaneComponent())
        }
        world.create {
            add(ModelInstanceComponent(factory.createGrid()))
        }
        world.create {
            add(ModelInstanceComponent(factory.createCursor()))
        }
        world.create {
            add(ModelInstanceComponent(factory.createAxes()))
        }
    }

    override fun show() {
        Gdx.input.inputProcessor = inputSystem
    }

    override fun hide() {
        Gdx.input.inputProcessor = null
    }

    override fun render(delta: Float) {
        clearScreen(0.2f, 0.2f, 0.2f)
        world.setDelta(delta)
        world.process()
    }


    override fun dispose() {
        world.dispose()
    }
}