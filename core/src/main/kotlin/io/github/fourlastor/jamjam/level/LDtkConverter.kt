package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import io.github.fourlastor.ldtk.LDtkLayerInstance
import io.github.fourlastor.ldtk.LDtkLevelDefinition

class LDtkConverter(private val scale: Float = 1f) {
  fun convert(levelDefinition: LDtkLevelDefinition): WorldLevel {
    return levelDefinition.toLevel()
  }

  private fun LDtkLevelDefinition.toLevel(): WorldLevel {
      return WorldLevel(
              layers = layerInstances.orEmpty().reversed().mapIndexedNotNull { i, it -> it.toLayer(i) })
  }

    private fun LDtkLayerInstance.toLayer(position: Int): WorldLevel.Layer? =
            when (type) {
                "AutoLayer" ->
                    tilesetRelPath
                            ?.let { Texture(Gdx.files.internal(it)) }
                            ?.let { texture ->
                                WorldLevel.Layer.SpriteLayer(
                                        position,
                                        texture,
                                        autoLayerTiles.map { tile ->
                                            Sprite(texture, tile.src[0], tile.src[1], gridSize, gridSize).apply {
                                                setScale(scale)
                                                setPosition(tile.px[0] * scale, tile.px[1] * scale)

                                                val flipFlags = tile.f
                                                val x = flipFlags and 1 == 1
                                                val y = flipFlags shr 1 and 1 == 1
                                                flip(x, !y)
                        }
                      })
                }
        else -> null
      }
}
