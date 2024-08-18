package map

import map.layers.MapLayer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.opengl.awt.AWTGLCanvas
import rendering.*
import java.awt.Cursor

class MapView : AWTGLCanvas() {
    private var layers: ArrayList<MapLayer> = ArrayList()
    lateinit var camera: Camera
    private lateinit var inputHandler: MapViewInputHandler

    init {
        cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
    }

    fun addLayer(layer: MapLayer) {
        layers.add(layer)
    }

    override fun initGL() {
        println("GL Version: ${effective.majorVersion}.${effective.minorVersion}")
        GL.createCapabilities()
        glClearColor(0f, 0.0f, 0.0f, 1.0f)

        camera = Camera(width.toFloat() / 100, height.toFloat() / 100)
        inputHandler = MapViewInputHandler(camera)

        addMouseMotionListener(inputHandler)
        addMouseWheelListener(inputHandler)
        addMouseListener(inputHandler)

        for (layer in layers) {
            layer.init(camera)
        }
    }

    override fun paintGL() {
        glViewport(0, 0, width, height)
        camera.updateViewport(width.toFloat() / 100, height.toFloat() / 100)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        for (layer in layers) {
            layer.render(camera)
        }

        swapBuffers()
    }
}