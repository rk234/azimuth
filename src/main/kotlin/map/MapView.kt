package map

import data.resources.ColormapTextureManager
import data.resources.ShaderManager
import map.layers.MapLayer
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import rendering.*
import java.awt.Cursor

class MapView(private val data: GLData?) : AWTGLCanvas(data) {
    constructor() : this(null)

    private var layers: ArrayList<MapLayer> = ArrayList()
    var camera: Camera
    private lateinit var inputHandler: MapViewInputHandler
    private val vaoContext: VAOContext = VAOContext()

    init {
        cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
        camera = Camera(width.toFloat() / 100, height.toFloat() / 100)
    }

    fun addLayer(layer: MapLayer) {
        layers.add(layer)
    }

    fun insertLayer(index: Int, layer: MapLayer) {
        layers.add(index, layer)
    }

    fun removeLayer(layer: MapLayer): MapLayer {
        layers.remove(layer)
//        layer.destroy()
        return layer
    }

    override fun initGL() {
        if(data?.shareContext == null)
            GL.createCapabilities()
        if(!ShaderManager.initialized)
            ShaderManager.init()

        if(!ColormapTextureManager.initialized)
            ColormapTextureManager.init()

        println("GL Version: ${effective.majorVersion}.${effective.minorVersion}")
        glClearColor(0f, 0.0f, 0.0f, 1.0f)

        inputHandler = MapViewInputHandler(camera, this)

        addMouseMotionListener(inputHandler)
        addMouseWheelListener(inputHandler)
        addMouseListener(inputHandler)

        for (layer in layers) {
            if(!layer.initialized())
                layer.init(camera, vaoContext)
        }
    }

    override fun paintGL() {
        glViewport(0, 0, width, height)
        camera.updateViewport(width.toFloat() / 100, height.toFloat() / 100)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        for (layer in layers) {
            if(!layer.initialized()) {
                layer.init(camera, vaoContext)
            }
            layer.render(camera, vaoContext)
        }

        swapBuffers()
    }
}