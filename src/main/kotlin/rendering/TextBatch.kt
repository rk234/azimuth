package rendering

import data.resources.ShaderManager
import org.joml.Vector2f
import org.joml.Vector3f

class TextBatch(
    private val color: Vector3f,
    private val size: Float,
    private val borderWidth: Float,
    private val borderColor: Vector3f
) {
    private val verts: List<Vector2f>
    private val indices: List<Int>
    private val shader = ShaderManager.instance.sdfTextShader()

    init {

    }

    fun addText(text: String, x: Float, y: Float, size: Float) {
        // Implementation to add text to the batch
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