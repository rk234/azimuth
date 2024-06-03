package rendering

import org.joml.Vector3f
import org.lwjgl.opengl.GL45.*
import java.io.File

class Renderer(val camera: Camera) {
    val radarShader: ShaderProgram
    val linesShader: ShaderProgram

    val radarVAO: VertexArrayObject
    val linesVAO: VertexArrayObject

    init {
        val radarVs = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val radarFs = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        radarShader = ShaderProgram()
        radarShader.createVertexShader(radarVs)
        radarShader.createFragmentShader(radarFs)
        radarShader.link()

        val linesVs = File("src/main/resources/shaders/lines/lines.vs.glsl").readText(Charsets.UTF_8)
        val linesFs = File("src/main/resources/shaders/lines/lines.fs.glsl").readText(Charsets.UTF_8)

        linesShader = ShaderProgram()
        linesShader.createVertexShader(linesVs)
        linesShader.createFragmentShader(linesFs)
        linesShader.link()

        radarVAO = VertexArrayObject()
        radarVAO.bind()

        radarVAO.attrib(0, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        radarVAO.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (1 * Float.SIZE_BYTES).toLong())
        radarVAO.attrib(2, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

        linesVAO = VertexArrayObject()
        linesVAO.bind()

        val stride = (2+1+2+2) * Float.SIZE_BYTES
        linesVAO.attrib(0, 2, GL_FLOAT, false, stride, 0)
        linesVAO.attrib(1, 1, GL_FLOAT, false, stride, (2*Float.SIZE_BYTES).toLong())
        linesVAO.attrib(2, 2, GL_FLOAT, false, stride, (3*Float.SIZE_BYTES).toLong())
        linesVAO.attrib(3, 2, GL_FLOAT, false, stride, (5*Float.SIZE_BYTES).toLong())
    }

    fun drawRadar(vbo: GLBufferObject, verts: Int, colormapTexture: Texture1D) {
        println("Here!!")
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vbo.bind()
        colormapTexture.bind()
        radarVAO.bind()
        radarVAO.enableAttrib(0)
        radarVAO.enableAttrib(1)
        radarVAO.enableAttrib(2)
        glDrawArrays(GL_TRIANGLES, 0, verts)
    }

    fun drawLines(vbo: GLBufferObject, verts: Int, color: Vector3f, width: Float) {
        linesShader.bind()
        linesShader.setUniformMatrix4f("projection", camera.projectionMatrix)
        linesShader.setUniformMatrix4f("transform", camera.transformMatrix)
        linesShader.setUniformFloat("aspect", camera.viewportDims.x/camera.viewportDims.y)
        linesShader.setUniformVec2f("resolution", camera.viewportDims)
        linesShader.setUniformFloat("thickness", width)
        linesShader.setUniformInt("miter", 0)
        linesShader.setUniformVec3f("color", color)

        vbo.bind()
        linesVAO.bind()
        linesVAO.enableAttrib(0)
        linesVAO.enableAttrib(1)
        linesVAO.enableAttrib(2)
        linesVAO.enableAttrib(3)
        glDrawArrays(GL_TRIANGLES, 0, verts)
    }
}