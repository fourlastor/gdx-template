package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLayerInstance
import io.github.fourlastor.ldtk.LDtkLevelDefinition

class LDtkConverter(private val scale: Float = 1f) {
    fun convert(levelDefinition: LDtkLevelDefinition, definitions: Definitions): WorldLevel {
        return levelDefinition.toLevel(definitions)
    }

    private fun LDtkLevelDefinition.toLevel(definitions: Definitions): WorldLevel {
        return WorldLevel(
            layers = layerInstances.orEmpty().reversed().mapIndexedNotNull { i, it -> it.toLayer(i, definitions) })
    }

    private fun LDtkLayerInstance.toLayer(position: Int, definitions: Definitions): WorldLevel.Layer? =
        when (type) {
            "AutoLayer" -> {
                val atlas = TextureAtlas(Gdx.files.internal("tiles.atlas"))
                definitions.tilesets.find { it.uid == tilesetDefUid }
                    ?.let { tileset ->
                        WorldLevel.Layer.SpriteLayer(
                            position,
                            atlas,
                            autoLayerTiles.mapNotNull { tile ->
                                tile.t
                                    .let { tileId -> tileset.customData.find { it.tileId == tileId } }
                                    ?.let { atlas.createSprite(it.data) }
                                    ?.apply {
                                        setOrigin(0f, 0f)
                                        setScale(scale)
                                        setPosition(tile.px[0] * scale, tile.px[1] * scale)

                                        val flipFlags = tile.f
                                        val x = flipFlags and 1 == 1
                                        val y = flipFlags shr 1 and 1 == 1
                                        flip(x, !y)
                                    }
                            })
                    }
            }
            else -> null
        }
}
