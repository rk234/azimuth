package map.layers

import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector4f
import rendering.Camera
import rendering.FontAtlas
import rendering.TextBatch
import rendering.VAOContext

class LabelLayer : MapLayer {
    private val atlas = FontAtlas("src/main/resources/fonts/Inter/static/Inter_24pt-Regular.ttf", 150f, 1024, 1024)
    private val batch = TextBatch(Vector4f(1f, 1f, 1f, 1f), 24f, 2f, Vector4f(0f, 0f, 0f, 1f), atlas)

    override fun init(camera: Camera, vaoContext: VAOContext) {
        batch.init(vaoContext)
        val mercator = MercatorProjection()
        val coord = mercator.toCartesian(Vector2f(40.7128f, -74.0060f)) // Example: New York City
        println("Adding label at coord: ${coord.x}, ${coord.y}")
        batch.addText("New York City", coord.x, coord.y,  0.01f)
        batch.flush()
        println("LabelLayer initialized")
    }

    override fun render(camera: Camera, vaoContext: VAOContext) {
        batch.draw(camera, vaoContext)
    }

    override fun destroy() {
        batch.destroy()
    }

    override fun initialized(): Boolean {
        return batch.initialized()
    }
}