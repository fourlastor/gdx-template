package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import io.github.fourlastor.ldtk.Definitions
import io.github.fourlastor.ldtk.LDtkLayerInstance
import io.github.fourlastor.ldtk.LDtkLevelDefinition

class LDtkConverter(private val scale: Float) {

    fun convert(levelDefinition: LDtkLevelDefinition, definitions: Definitions): Level {
        val layerInstances = levelDefinition.layerInstances.orEmpty().reversed()
        return Level(
            statics = LevelStatics(
                spriteLayers = layerInstances
                    .mapIndexedNotNull { i, it -> it.toLayer(i, definitions) },
                staticBodies = layerInstances.firstOrNull { it.type == "IntGrid" }
                    .toBoxes()
            ),
            player = layerInstances
                .indexOfFirst { it.type == "Entities" }
                .let { checkNotNull(it.takeIf { it >= 0 }) { "Entities layer missing from level." } }
                .let { layerIndex ->
                    val layer = layerInstances[layerIndex]
                    layer.entityInstances
                        .firstOrNull { it.identifier == "Player" }
                        .let { checkNotNull(it) { "Player missing from entity layer." } }
                        .let {
                            val atlas = TextureAtlas(Gdx.files.internal("entities.atlas"))
                            Player(
                                atlas = atlas,
                                sprite = atlas.createSprite("player-stand").apply {
                                    setOrigin(0f, 0f)
                                    setScale(scale)
                                    setPosition(it.px[0] * scale, it.px[1] * scale)
                                    flip(false, true)
                                },
                                layerIndex = layerIndex
                            )
                        }
                }


        )
    }

    /** Converts an IntGrid layer to definitions used in the physics world. */
    private fun LDtkLayerInstance?.toBoxes(): List<Rectangle> = this?.run {

        fun Int.x() = (this % cWid).toFloat()
        fun Int.y() = (this / cWid).toFloat()

        intGridCSV.orEmpty()
            .mapIndexedNotNull { index, i ->
                index.takeIf { i == 1 }?.let {
                    Rectangle(
                        index.x(),
                        index.y(),
                        gridSize * scale,
                        gridSize * scale,
                    )
                }
            }
    }.orEmpty()

    private fun LDtkLayerInstance.toLayer(position: Int, definitions: Definitions): SpriteLayer? =
        when (type) {
            "AutoLayer" -> {
                val atlas = TextureAtlas(Gdx.files.internal("tiles.atlas"))
                definitions.tilesets.find { it.uid == tilesetDefUid }
                    ?.let { tileset ->
                        SpriteLayer(
                            atlas,
                            position,
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
