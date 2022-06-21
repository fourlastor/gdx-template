package io.github.fourlastor.jamjam.level.system

import com.badlogic.gdx.graphics.Camera
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import ktx.graphics.update

@AllOf([KinematicBodyComponent::class, PlayerComponent::class])
class CameraFollowPlayerSystem(
    private val camera: Camera,
    private val bodies: ComponentMapper<KinematicBodyComponent>,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val center = bodies[entity].body.position
        camera.update { position.x = center.x }
    }
}
