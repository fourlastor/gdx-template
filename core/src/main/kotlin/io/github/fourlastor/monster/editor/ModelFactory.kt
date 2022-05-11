package io.github.fourlastor.monster.editor

import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.utils.Disposable

class ModelFactory : Disposable {
    private val plane = ModelBuilder()
        .createRect(
            5f, 0f, -5f,
            -5f, 0f, -5f,
            -5f, 0f, 5f,
            5f, 0f, 5f,
            0f, 1f, 0f,
            color(0.4f, 0.4f, 0.4f, 0.1f),
            VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.ColorPacked.toLong()
        )

    private val grid = ModelBuilder()
        .createLineGrid(
            100,
            100,
            1f,
            1f,
            color(1f, 1f, 1f, 0.05f),
            VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.ColorPacked.toLong(),
        )

    private val cursor = axesLines(color(1f, 0f, 0.4f, 1f), 1f)

    private val axes = axesLines(color(1f, 1f, 1f, 0.1f), 10f)

    override fun dispose() {
        plane.dispose()
        grid.dispose()
        cursor.dispose()
        axes.dispose()
    }


    fun createPlane() = ModelInstance(plane)

    fun createGrid() = ModelInstance(grid)

    fun createCursor(): ModelInstance = ModelInstance(cursor)

    fun createAxes(): ModelInstance = ModelInstance(axes)

    private fun color(r: Float, g: Float, b: Float, a: Float) = Material(
        ColorAttribute.createDiffuse(r, g, b, a),
        BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA),
    )

    private fun axesLines(material: Material, length: Float) =
        ModelBuilder().run {
            begin()
            part(
                "xyz",
                GL20.GL_LINES,
                VertexAttributes.Usage.Position.toLong() or VertexAttributes.Usage.ColorPacked.toLong(),
                material
            ).apply {
                line(-length, 0f, 0f, length, 0f, 0f)
                line(0f, -length, 0f, 0f, length, 0f)
                line(0f, 0f, -length, 0f, 0f, length)
            }

            end()
        }

}