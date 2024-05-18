package map

import org.joml.Vector2f
import rendering.Camera
import java.awt.event.*
import java.util.Vector

class MapViewInputHandler(camera: Camera) : MouseListener, MouseWheelListener, MouseMotionListener {
    private var prevMousPos: Vector2f = Vector2f(-1f);
    private val camera: Camera

    init {
        this.camera = camera
    }

    override fun mouseClicked(e: MouseEvent?) {
    }

    override fun mousePressed(e: MouseEvent?) {
        prevMousPos = Vector2f(e?.x?.toFloat() ?: 0f, e?.y?.toFloat() ?: 0f)
    }

    override fun mouseReleased(e: MouseEvent?) {
    }

    override fun mouseEntered(e: MouseEvent?) {
    }

    override fun mouseExited(e: MouseEvent?) {
    }

    override fun mouseWheelMoved(e: MouseWheelEvent?) {
        camera.zoom += (e?.preciseWheelRotation?.toFloat() ?: 0f) / 50f
        println(camera.zoom)
        camera.recalcProjection()
    }

    override fun mouseDragged(e: MouseEvent?) {
        val delta = Vector2f(e?.x?.minus(prevMousPos.x) ?: 0f, e?.y?.minus(prevMousPos.y) ?: 0f)
        camera.translate(delta.mul(0.01f / camera.zoom).mul(Vector2f(-1f, 1f)))
        prevMousPos = Vector2f(e?.x?.toFloat() ?: 0f, e?.y?.toFloat() ?: 0f)
    }

    override fun mouseMoved(e: MouseEvent?) {
    }
}