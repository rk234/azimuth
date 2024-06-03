package map.layers

import RadarVolume
import org.lwjgl.opengl.GL45.*;
import meteo.radar.Product
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File

class RadarLayer(val volume: RadarVolume) : MapLayer {
    private lateinit var vbo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var radarShader: ShaderProgram
    private lateinit var colormapTexture: Texture1D
    private lateinit var camera: Camera
    private var numVerts: Int = 0

    override fun init(camera: Camera) {
        val vsSource = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        radarShader = ShaderProgram()
        radarShader.createVertexShader(vsSource)
        radarShader.createFragmentShader(fsSource)
        radarShader.link()

        vao = VertexArrayObject()
        vao.bind()

        val verts = MemoryUtil.memAllocFloat(720 * 1832 * 3 * 6)

        vbo = GLBufferObject()
        vbo.bind()

        verts.flip()
        vbo.uploadData(verts, GL_STATIC_DRAW)
        MemoryUtil.memFree(verts)

        vao.attrib(0, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (1 * Float.SIZE_BYTES).toLong())
        vao.attrib(2, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        vao.enableAttrib(2)
    }

    override fun render(renderer: Renderer) {
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vbo.bind()
        colormapTexture.bind()
        vao.bind()
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        vao.enableAttrib(2)
        glDrawArrays(GL_TRIANGLES, 0, numVerts)
    }

    override fun destroy() {
        vbo.destroy()
        vao.destroy()
        radarShader.destroy()
    }

    fun loadColormapFromProduct(product: Product) {
        when(product) {
            Product.REFLECTIVITY_HIRES -> TODO()
            Product.RADIAL_VEL_HIRES -> TODO()
            Product.CORRELATION_COEF_HIRES -> TODO()
        }
    }
}