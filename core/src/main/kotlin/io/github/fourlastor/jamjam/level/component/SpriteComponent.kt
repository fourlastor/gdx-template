package io.github.fourlastor.jamjam.level.component

import com.artemis.Component
import com.badlogic.gdx.graphics.g2d.Sprite

class SpriteComponent : Component() {
    lateinit var sprite: Sprite
    var priority: Int = -1
}
