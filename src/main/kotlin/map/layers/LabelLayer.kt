package map.layers

import com.google.common.io.Files
import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector4f
import rendering.AABB
import rendering.Camera
import rendering.FontAtlas
import rendering.TextBatch
import rendering.VAOContext
import java.io.File

data class Label(
    val text: String,
    val position: Vector2f,
    val population: Int,
    val size: Float
)

class LabelLayer : MapLayer {
    private val atlas = FontAtlas("src/main/resources/fonts/IBMPlexSans_Condensed-Bold.ttf", 150f, 1024, 1024)
    private val batch = TextBatch(Vector4f(1f, 1f, 1f, 1f), 24f, 4f, Vector4f(0f, 0f, 0f, 1f), atlas)

    private val labels = mutableListOf<Label>()
    private var lastViewBounds: AABB? = null
    private var lastZoom: Float = -1f

    override fun init(camera: Camera, vaoContext: VAOContext) {
        val citiesCSV = Files.readLines(File("src/main/resources/geo/USCities.csv"), Charsets.UTF_8)

        val mercator = MercatorProjection()
        batch.init(vaoContext)

        // Parse and store all labels
        for (line in citiesCSV) {
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
            labels.add(Label(city, coord, population, size))
        }

        println("LabelLayer initialized with ${labels.size} labels")
    }

    private fun getPopulationThreshold(zoom: Float): Int {
        return when {
            zoom < 0.00005f/10 -> 1_000_000   // Very zoomed out: only major cities
            zoom < 0.0001f/10 -> 500_000      // Zoomed out: large cities
            zoom < 0.0005f/10 -> 100_000      // Medium zoom: medium cities
            zoom < 0.001f/10 -> 50_000        // Closer: smaller cities
            else -> 0                       // Zoomed in: show all
        }
    }

    private fun rebuildBatch(camera: Camera) {
        val viewBounds = camera.getViewBounds(1.4f)

        batch.clear()

        // Track placed label bounding boxes in world space for overlap detection
        val placedLabels = mutableListOf<AABB>()
        val labelPadding = 0.5f  // Pixel padding between labels

        // Filter and sort labels by population (descending) so larger cities get priority
        val candidateLabels = labels
            .filter { viewBounds.contains(it.position) }
            .sortedByDescending { it.population }

        var visibleCount = 0
        for (label in candidateLabels) {
            // Work in world coordinates for overlap detection
            // The label renders at constant pixel size on screen, but we need to check overlap in world space

            // Get label bounds in world space
            // Text geometry in addText: localPos = stringBounds * label.size
            // Shader does: localPos / zoom, giving world-space offset from anchor
            // So world-space label size = stringBounds * label.size / zoom
            val bounds = atlas.stringBounds(label.text)
            val worldWidth = bounds.x * label.size / camera.zoom
            val worldHeight = bounds.y * label.size / camera.zoom

            // Create world-space AABB for this label (centered on position)
            // Convert pixel padding to world space as well
            val paddingWorld = labelPadding / camera.zoom
            val halfWidth = worldWidth / 2f + paddingWorld
            val halfHeight = worldHeight / 2f + paddingWorld
            val labelAABB = AABB(
                topLeft = Vector2f(label.position.x - halfWidth, label.position.y + halfHeight),
                bottomRight = Vector2f(label.position.x + halfWidth, label.position.y - halfHeight)
            )

            // Check for overlap with already placed labels
            var overlaps = false
            for (placed in placedLabels) {
                if (labelAABB.intersects(placed)) {
                    overlaps = true
                    break
                }
            }

            if (overlaps) continue

            // No overlap - place this label
            placedLabels.add(labelAABB)
            batch.addText(label.text, label.position.x, label.position.y, label.size)
            visibleCount++
        }

        batch.flush()
        lastViewBounds = viewBounds
        lastZoom = camera.zoom
    }

    private fun needsRebuild(camera: Camera): Boolean {
        val currentBounds = camera.getViewBounds()

        // Always rebuild if we haven't built yet
        if (lastViewBounds == null) return true

        // Rebuild if zoom changed (affects population threshold)
        if (lastZoom != camera.zoom) return true

        // Rebuild if view bounds changed significantly
        val last = lastViewBounds!!
        val threshold = 0.01f * (1f / camera.zoom) // Small threshold based on zoom

        if (kotlin.math.abs(currentBounds.topLeft.x - last.topLeft.x) > threshold ||
            kotlin.math.abs(currentBounds.topLeft.y - last.topLeft.y) > threshold ||
            kotlin.math.abs(currentBounds.bottomRight.x - last.bottomRight.x) > threshold ||
            kotlin.math.abs(currentBounds.bottomRight.y - last.bottomRight.y) > threshold) {
            return true
        }

        return false
    }

    override fun render(camera: Camera, vaoContext: VAOContext) {
        if (needsRebuild(camera)) {
            rebuildBatch(camera)
        }
        batch.draw(camera, vaoContext)
    }

    override fun destroy() {
        batch.destroy()
    }

    override fun initialized(): Boolean {
        return batch.initialized()
    }
}
