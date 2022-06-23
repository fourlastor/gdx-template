package io.github.fourlastor.jamjam.level.component

import com.artemis.Component
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle

class RenderComponent: Component() {
    lateinit var render: Render
}


sealed class Render {

    abstract fun draw(batch: SpriteBatch)
    abstract fun increaseTime(delta: Float)

    abstract val dimensions: Rectangle

    class Blueprint(override val dimensions: Rectangle): Render() {
        override fun draw(batch: SpriteBatch) = Unit
        override fun increaseTime(delta: Float) = Unit
    }

    class SpriteRender(
        private val sprite: Sprite,
    ): Render() {
        override fun draw(batch: SpriteBatch) {
            sprite.draw(batch)
        }

        override fun increaseTime(delta: Float) = Unit

        override val dimensions: Rectangle
            get() = sprite.boundingRectangle
    }

    class AnimationRender(
        private val animation: Animation<Sprite>,
        override val dimensions: Rectangle,
    ): Render() {

        private var delta: Float = 0f
        override fun draw(batch: SpriteBatch) {
            val texture = animation.getKeyFrame(delta)
            batch.draw(texture, dimensions.x, dimensions.y, dimensions.width, dimensions.height)
        }

        override fun increaseTime(delta: Float) {
            this.delta += delta
        }
    }
}
