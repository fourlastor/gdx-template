package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Disposable
import java.lang.Float.max
import kotlin.math.min

class WorldLevel(val layers: List<Layer>) : Disposable {

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
        var minx = Float.MAX_VALUE
        var miny = Float.MAX_VALUE
        var maxx = Float.MIN_VALUE
        var maxy = Float.MIN_VALUE
        tiles.forEach {
          it.draw(batch)
          minx = min(minx, it.x)
          miny = min(miny, it.y)
          maxx = max(maxx, it.x)
          maxy = max(maxy, it.y)
        }
      }

      override fun dispose() {
        texture.dispose()
      }
    }
  }
}
