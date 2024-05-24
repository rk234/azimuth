package map

import RadarVolume
import meteo.radar.Product
import org.joml.Vector2f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import org.lwjgl.system.MemoryUtil
import rendering.Camera
import rendering.GLBufferObject
import rendering.ShaderProgram
import rendering.VertexArrayObject
import ucar.nc2.NetcdfFiles

import java.io.File

class MapView(data: GLData?) : AWTGLCanvas(data) {
    lateinit var vertexBuffer: GLBufferObject;
    lateinit var vertexArrayObject: VertexArrayObject;
    lateinit var shader: ShaderProgram;
    lateinit var camera: Camera
    lateinit var inputHandler: MapViewInputHandler
    var numVerts = 0;

    override fun initGL() {
        println("GL Version: ${effective.majorVersion}.${effective.minorVersion}")
        GL.createCapabilities()
        glClearColor(0f, 0.0f, 0.0f, 1.0f)

        camera = Camera(width.toFloat(), height.toFloat())
        inputHandler = MapViewInputHandler(camera)

        addMouseMotionListener(inputHandler)
        addMouseWheelListener(inputHandler)
        addMouseListener(inputHandler)

        val vsSource = File("src/main/resources/shaders/default/default.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/default/default.fs.glsl").readText(Charsets.UTF_8)

        shader = ShaderProgram()
        shader.createVertexShader(vsSource)
        shader.createFragmentShader(fsSource)
        shader.link()

        val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
        val vol = RadarVolume(file, Product.REFLECTIVITY_HIRES)
        println(vol.station)
        println(vol.latitude)
        println(vol.longitude)

        val verts = MemoryUtil.memAllocFloat(720*1832*6*6)

        val firstScan = vol.scans[0]

        val resolution = Math.toRadians(0.5) //TODO: Should be 1 degree for normal resolution, .5 for super-res
        var gateSize: Float = -1f
        for((radialIndex, radial) in firstScan.radials.withIndex()) {
            for((gateIndex, gate) in radial.withIndex()) {
                val azimuth = gate.azimuth-90
                val range = gate.range / 1000
                val data = gate.data

                val startAngle = Math.toRadians(azimuth.toDouble()) - (resolution/2)
                val endAngle = Math.toRadians(azimuth.toDouble()) + (resolution/2)

                if(gateSize == -1f) {
                    gateSize = (radial[gateIndex+1].range-radial[gateIndex].range)/1000
                }

                val p1 = Vector2f(
                    (Math.cos(startAngle) * range).toFloat(),
                    (Math.sin(startAngle) * range).toFloat()
                )
                val p2 = Vector2f(
                    (Math.cos(startAngle) * (range+gateSize)).toFloat(),
                    (Math.sin(startAngle) * (range+gateSize)).toFloat()
                )
                val p3 = Vector2f(
                    (Math.cos(endAngle) * (range+gateSize)).toFloat(),
                    (Math.sin(endAngle) * (range+gateSize)).toFloat()
                )
                val p4 = Vector2f(
                    (Math.cos(endAngle) * range).toFloat(),
                    (Math.sin(endAngle) * range).toFloat()
                )

                verts.put(floatArrayOf(p1.x, p1.y, 0f, 1f, 0f, 0f))
                verts.put(floatArrayOf(p2.x, p2.y, 0f, 1f, 0f, 0f))
                verts.put(floatArrayOf(p3.x, p3.y, 0f, 1f, 0f, 0f))

                verts.put(floatArrayOf(p3.x, p3.y, 0f, 1f, 0f, 0f))
                verts.put(floatArrayOf(p4.x, p4.y, 0f, 1f, 0f, 0f))
                verts.put(floatArrayOf(p1.x, p1.y, 0f, 1f, 0f, 0f))
                numVerts+=6
            }
        }

//        val verts = floatArrayOf(
//            0.0f, 0.5f, 1.0f, 1.0f, 0f, 0f,
//            -0.5f, -0.5f, 1.0f, 0f, 1.0f, 0f,
//            0.5f, -0.5f, 1.0f, 0f, 0f, 1.0f
//        )
        verts.flip()

        vertexArrayObject = VertexArrayObject()
        vertexArrayObject.bind()

        vertexBuffer = GLBufferObject()
        vertexBuffer.bind()
        vertexBuffer.uploadData(verts, GL_STATIC_DRAW)

        MemoryUtil.memFree(verts)

        vertexArrayObject.attrib(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
        vertexArrayObject.attrib(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
    }

    override fun paintGL() {
        glViewport(0, 0, width, height)
        shader.bind()

        camera.updateViewport(width.toFloat() / 100, height.toFloat() / 100)
        shader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        shader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vertexBuffer.bind()
        vertexArrayObject.bind()
        vertexArrayObject.enableAttrib(0)
        vertexArrayObject.enableAttrib(1)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glDrawArrays(GL_TRIANGLES, 0, numVerts)
        swapBuffers()
    }
}