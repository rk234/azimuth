package map.layers

import com.google.common.io.Files
import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector4f
import rendering.Camera
import rendering.FontAtlas
import rendering.TextBatch
import rendering.VAOContext
import java.io.File

class LabelLayer : MapLayer {
    private val atlas = FontAtlas("src/main/resources/fonts/IBMPlexSans_Condensed-Bold.ttf", 150f, 1024, 1024)
    private val batch = TextBatch(Vector4f(1f, 1f, 1f, 1f), 24f, 4f, Vector4f(0f, 0f, 0f, 1f), atlas)

    override fun init(camera: Camera, vaoContext: VAOContext) {
        val citiesCSV = Files.readLines(File("src/main/resources/geo/USCities.csv"), Charsets.UTF_8)

        val mercator = MercatorProjection()
        batch.init(vaoContext)
        for(line in citiesCSV) {
            val parts = line.split(",")
            val city = parts[0]
            val lat = parts[1].toFloat()
            val lon = parts[2].toFloat()
            val population = parts[3].toInt()
            val size = when {
                population > 1_000_000 -> 0.004f
                population > 500_000 -> 0.003f
                population > 100_000 -> 0.0025f
                else -> 0.002f
            }

            val coord = mercator.toCartesian(Vector2f(lat, lon))
            batch.addText(city, coord.x, coord.y,  size)
        }
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
