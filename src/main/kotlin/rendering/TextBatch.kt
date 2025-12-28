package rendering

import data.resources.ShaderManager
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.stb.STBTruetype

class TextBatch(
    private val color: Vector3f,
    private val size: Float,
    private val borderWidth: Float,
    private val borderColor: Vector3f,
    private val atlas: FontAtlas
) {
    private val verts: List<Vector2f>
    private val indices: List<Int>
    private val shader = ShaderManager.instance.sdfTextShader()

    init {
        verts = mutableListOf()
        indices = mutableListOf()
    }

    fun addText(text: String, x: Float, y: Float, size: Float) {
        for (char in text) {
//            val bakedChar = atlas.bakedChar(char) ?: continue

        }
    }

    fun flush() {
        // Implementation to flush the text batch to the GPU
    }

    fun render() {
        // Implementation to render the text batch
    }

    fun destroy() {
        // Implementation to clean up resources
    }
}