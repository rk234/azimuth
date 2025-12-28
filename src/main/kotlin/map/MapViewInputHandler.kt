package map

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import rendering.Camera
import java.awt.event.*
import java.util.Vector

class MapViewInputHandler(private val camera: Camera, private var view: MapView) : MouseListener, MouseWheelListener, MouseMotionListener {
    private var prevMousPos: Vector2f = Vector2f(-1f);

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
        val rot = (e?.wheelRotation ?: 0).toFloat()
        if (rot == 0f) return

        val mouseX = (e?.x ?: 0).toFloat()
        val mouseY = (e?.y ?: 0).toFloat()

        val targetPoint = pixelToCameraCoords(Vector2f(mouseX, mouseY))

        val zoomDelta = if (rot > 0f) -camera.zoom * 0.05f else camera.zoom * 0.05f
        camera.zoomTowards(targetPoint, zoomDelta)
    }

    private fun pixelToCameraCoords(pixelPos: Vector2f): Vector2f {
        val camX = (pixelPos.x - view.width / 2f) / 100f
        val camY = (view.height / 2f - pixelPos.y) / 100f
        return Vector2f(camX, camY)
    }

    override fun mouseDragged(e: MouseEvent?) {
        val delta = Vector2f(e?.x?.minus(prevMousPos.x) ?: 0f, e?.y?.minus(prevMousPos.y) ?: 0f)
        camera.translate(delta.mul(0.01f / camera.zoom).mul(Vector2f(-1f, 1f)))
        println("camera pos: ${camera.position}")
        prevMousPos = Vector2f(e?.x?.toFloat() ?: 0f, e?.y?.toFloat() ?: 0f)
    }

    override fun mouseMoved(e: MouseEvent?) {
    }
}