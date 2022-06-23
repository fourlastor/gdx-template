package io.github.fourlastor.jamjam.level

import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Disposable

class Level(
    val statics: LevelStatics,
    val player: Player,
): Disposable {
    override fun dispose() {
        statics.dispose()
        player.dispose()
    }
}

class LevelStatics(
    val spriteLayers: List<SpriteLayer>,
    val staticBodies: List<Rectangle>,
): Disposable {
    override fun dispose() = spriteLayers.forEach {it.dispose() }
}

class SpriteLayer(
    private val atlas: TextureAtlas,
    val tiles: List<Sprite>
): Disposable {
    override fun dispose() = atlas.dispose()
}

class Player(
    val atlas: TextureAtlas,
    val dimensions: Rectangle,
): Disposable {
    override fun dispose() = atlas.dispose()
}
