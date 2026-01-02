package map.layers

import data.resources.FontManager
import org.joml.Vector2f
import org.joml.Vector4f
import rendering.AABB
import rendering.Camera
import rendering.TextBatch
import rendering.VAOContext

data class Label(
    val text: String,
    val position: Vector2f,
    val priority: Int,
    val size: Float
)

class LabelLayer(
    private val labels: MutableList<Label>,
    color: Vector4f = Vector4f(1f, 1f, 1f, 1f),
    borderWidth: Float = 4f,
    borderColor: Vector4f = Vector4f(0f, 0f, 0f, 1f),
    size: Float = 24f
) : MapLayer {
    private val atlas = FontManager.instance.getDefaultFont()
    private val batch = TextBatch(
        color,
        size,
        borderWidth,
        borderColor,
        atlas
    )

    private var lastViewBounds: AABB? = null
    private var lastZoom: Float = -1f

    override fun init(camera: Camera, vaoContext: VAOContext) {
        batch.init(vaoContext)

        println("LabelLayer initialized with ${labels.size} labels")
    }

    fun setLabels(newLabels: List<Label>) {
        labels.clear()
        labels.addAll(newLabels)
        lastViewBounds = null // Force rebuild
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
            .sortedByDescending { it.priority }

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
