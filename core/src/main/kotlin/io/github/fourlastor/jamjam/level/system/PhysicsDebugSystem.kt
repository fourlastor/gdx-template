package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.fleks.IntervalSystem

class PhisycsDebugSystem(
    private val camera: Camera,
    private val box2dWorld: World,
) : IntervalSystem() {

    private val debugRenderer = Box2DDebugRenderer()

    override fun onTick() {
        debugRenderer.render(box2dWorld, camera.combined)
    }

    override fun onDispose() {
        debugRenderer.dispose()
    }

}
