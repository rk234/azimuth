package rendering

import map.projection.MercatorProjection
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f

class Camera(viewWidth: Float, viewHeight: Float) {
    lateinit var projectionMatrix: Matrix4f
    lateinit var transformMatrix: Matrix4f

    var position: Vector3f = Vector3f(0f)
        set(newVal) {
            field = newVal
            recalcTransform()
        }
    var zoom: Float = 1f
        set(newVal) {
            field = newVal
            recalcTransform()
        }

    var viewportDims: Vector2f

    init {
        position = Vector3f(0f);
        viewportDims = Vector2f(viewWidth, viewHeight)
        zoom = 1f
        recalcTransform()
        recalcProjection()
    }

    fun recalcTransform() {
        transformMatrix = Matrix4f().translate(Vector3f(position).mul(-1f))
    }

    fun updateViewport(width: Float, height: Float) {
        viewportDims = Vector2f(width, height)
        recalcProjection()
    }

    fun recalcProjection() {
        projectionMatrix = Matrix4f().ortho(
            -viewportDims.x * (1 / zoom) / 2,
            viewportDims.x * (1 / zoom) / 2,
            -viewportDims.y * (1 / zoom) / 2,
            viewportDims.y * (1 / zoom) / 2,
            -1f,
            1f
        )
    }

    fun translate(delta: Vector2f) {
        position.add(Vector3f(delta, position.z))
        recalcTransform()
    }

    fun zoomTowards(targetPoint: Vector2f, zoomDelta: Float) {
        val oldZoom = zoom
        val newZoom = (zoom + zoomDelta).coerceIn(1e-6f, 100f)

        if (newZoom == oldZoom) return

        // The target point under the cursor stays fixed during zoom
        // We need to calculate how much to move the camera to keep it fixed
        //
        // Screen-to-world conversion factor changes with zoom:
        // At oldZoom: screenOffset * (1/oldZoom) = worldOffset
        // At newZoom: screenOffset * (1/newZoom) = worldOffset
        //
        // The difference in world offset for the same screen point:
        val scaleFactor = (1f / oldZoom) - (1f / newZoom)
        val offsetX = targetPoint.x * scaleFactor
        val offsetY = targetPoint.y * scaleFactor

        position = Vector3f(position.x + offsetX, position.y + offsetY, position.z)
        zoom = newZoom
    }
}