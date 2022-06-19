package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable

class WorldLevel(
    val layers: List<Layer>,
    val boxes: List<Rectangle>,
) : Disposable {

    override fun dispose() {
        layers.forEach { it.dispose() }
    }

    sealed class Layer : Disposable {

        abstract val order: Int

        abstract fun render(batch: SpriteBatch)

        class SpriteLayer(
            override val order: Int,
            private val texture: TextureAtlas,
            private val tiles: List<Sprite>
        ) : Layer() {

            override fun render(batch: SpriteBatch) {
                tiles.forEach {
                    it.draw(batch)
                }
            }

            override fun dispose() {
                texture.dispose()
            }
        }
    }
}
