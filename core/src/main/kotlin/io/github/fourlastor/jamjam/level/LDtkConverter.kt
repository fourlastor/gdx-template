package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLayerInstance
import io.github.fourlastor.ldtk.LDtkLevelDefinition

class LDtkConverter(private val scale: Float) {
    fun convert(levelDefinition: LDtkLevelDefinition, definitions: Definitions): WorldLevel = WorldLevel(
        layers = levelDefinition.layerInstances.orEmpty().reversed()
            .mapIndexedNotNull { i, it -> it.toLayer(i, definitions) },
        boxes = levelDefinition.layerInstances?.firstOrNull { it.type == "IntGrid" }
            .toBoxes()
    )

    /** Converts an IntGrid layer to definitions used in the physics world. */
    private fun LDtkLayerInstance?.toBoxes(): List<Rectangle> = this?.run {
        intGridCSV.orEmpty()
            .mapIndexedNotNull { index, i ->
                index.takeIf { i == 1 }?.let {
                    Rectangle(
                        (index % cWid).toFloat(),
                        (index / cWid).toFloat(),
                        gridSize * scale,
                        gridSize * scale,
                    )
                }
            }
    }.orEmpty()

    /** Converts an AutoLayer to a renderable [WorldLevel.Layer]. */
    private fun LDtkLayerInstance.toLayer(position: Int, definitions: Definitions): WorldLevel.Layer? =
        when (type) {
            "Entities" -> {
                val atlas = TextureAtlas(Gdx.files.internal("entities.atlas"))
                WorldLevel.Layer.SpriteLayer(
                    position,
                    atlas,
                    entityInstances.map {
                        atlas.createSprite("player-stand").apply {
                            setOrigin(0f, 0f)
                            setScale(scale)
                            setPosition(it.px[0] * scale, it.px[1] * scale)
                            flip(false, true)
                        }
                    }
                )
            }
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
