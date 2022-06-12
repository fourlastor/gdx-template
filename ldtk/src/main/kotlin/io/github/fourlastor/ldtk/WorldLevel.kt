package io.github.fourlastor.ldtk

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Disposable
import java.lang.Float.max
import kotlin.math.min

class WorldLevel(private val layers: List<Layer>) {
  fun render(batch: SpriteBatch) {
    layers.forEach { it.render(batch) }
  }
  sealed class Layer {

    abstract fun render(batch: SpriteBatch)

    class SpriteLayer(private val texture: Texture, private val tiles: List<Sprite>) :
        Disposable, Layer() {

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
