package map

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import org.lwjgl.system.MemoryUtil
import rendering.Camera
import rendering.GLBufferObject
import rendering.ShaderProgram
import rendering.VertexArrayObject

import java.io.File

class MapView(data: GLData?) : AWTGLCanvas(data) {
    lateinit var vertexBuffer: GLBufferObject;
    lateinit var vertexArrayObject: VertexArrayObject;
    lateinit var shader: ShaderProgram;
    lateinit var camera: Camera
    lateinit var inputHandler: MapViewInputHandler

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

        println(camera.projectionMatrix)

        val verts = floatArrayOf(
            0.0f, 0.5f, 1.0f, 1.0f, 0f, 0f,
            -0.5f, -0.5f, 1.0f, 0f, 1.0f, 0f,
            0.5f, -0.5f, 1.0f, 0f, 0f, 1.0f
        )
        val floatBuffer = MemoryUtil.memAllocFloat(verts.size)
        floatBuffer.put(verts).flip()

        vertexArrayObject = VertexArrayObject()
        vertexArrayObject.bind()

        vertexBuffer = GLBufferObject()
        vertexBuffer.bind()
        vertexBuffer.uploadData(floatBuffer, GL_STATIC_DRAW)

        MemoryUtil.memFree(floatBuffer)

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
        glDrawArrays(GL_TRIANGLES, 0, 3)
        swapBuffers()
    }
}