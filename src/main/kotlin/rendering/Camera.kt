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
}