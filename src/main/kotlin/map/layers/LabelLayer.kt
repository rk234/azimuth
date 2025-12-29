package map.layers

import org.joml.Vector4f
import rendering.Camera
import rendering.FontAtlas
import rendering.TextBatch
import rendering.VAOContext

class LabelLayer : MapLayer {
    private val atlas = FontAtlas("src/main/resources/fonts/Inter/static/Inter_24pt-Regular.ttf", 150f, 1024, 1024)
    private val batch = TextBatch(Vector4f(1f, 1f, 1f, 1f), 24f, 1f, Vector4f(0f, 0f, 0f, 1f), atlas)

    override fun init(camera: Camera, vaoContext: VAOContext) {
        batch.init(vaoContext)
        batch.addText("Hello, World!", 0f, 0f, 0.001f)
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