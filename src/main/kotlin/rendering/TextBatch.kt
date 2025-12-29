package rendering

import data.resources.ShaderManager
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryUtil

class TextBatch(
    private val color: Vector4f,
    private val size: Float,
    private val borderWidth: Float,
    private val borderColor: Vector4f,
    private val atlas: FontAtlas,
) : Renderable {
    private val verts: MutableList<Vector4f> = mutableListOf()
    private val indices: MutableList<Int> = mutableListOf()
    private lateinit var shader: ShaderProgram
    private lateinit var vaoContext: VAOContext

    private var initialized = false

    private lateinit var vbo: GLBufferObject
    private lateinit var ibo: GLBufferObject

    fun addText(text: String, x: Float, y: Float, size: Float) {
        val cursor = Vector2f(x, y)
        for (char in text) {
            val glyph = atlas.getGlyph(char) ?: continue

            val x0 = cursor.x + glyph.xOffset * size
            val y0 = cursor.y - glyph.yOffset * size
            val x1 = x0 + glyph.width * size
            val y1 = y0 - glyph.height * size

            verts.add(Vector4f(x0, y0, glyph.u0, glyph.v0))
            verts.add(Vector4f(x1, y0, glyph.u1, glyph.v0))
            verts.add(Vector4f(x1, y1, glyph.u1, glyph.v1))
            verts.add(Vector4f(x0, y1, glyph.u0, glyph.v1))

            indices.addAll(listOf(
                verts.size - 4, verts.size - 3, verts.size - 2,
                verts.size - 4, verts.size - 2, verts.size - 1
            ))

            cursor.x += glyph.advance * size
        }
    }

    fun flush() {
        val vao = vaoContext.getVAO(this)
        vao.bind()
        val vertBuffer = MemoryUtil.memAllocFloat(verts.size * 4)
        val indexBuffer = MemoryUtil.memAllocInt(indices.size)

        for (v in verts) {
            vertBuffer.put(v.x).put(v.y).put(v.z).put(v.w)
        }
        vertBuffer.flip()
        for (i in indices) {
            indexBuffer.put(i)
        }
        indexBuffer.flip()

        vbo = GLBufferObject()
        vbo.bind()
        vbo.uploadData(vertBuffer, GL45.GL_STATIC_DRAW)

        ibo = GLBufferObject(GL45.GL_ELEMENT_ARRAY_BUFFER)
        ibo.bind()
        ibo.uploadData(indexBuffer, GL45.GL_STATIC_DRAW)

        vao.attrib(0, 2, GL45.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 2, GL45.GL_FLOAT, false, 4 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
        vao.enableAttrib(0)
        vao.enableAttrib(1)

        MemoryUtil.memFree(vertBuffer)
        MemoryUtil.memFree(indexBuffer)
    }

    override fun init(vaoContext: VAOContext) {
        this.vaoContext = vaoContext
        this.shader = ShaderManager.instance.sdfTextShader()
        initialized = true
        println("TextBatch initialized")
    }

    override fun draw(camera: Camera, vaoContext: VAOContext) {
        if(!initialized) return
        shader.bind()
        shader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        shader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)
        shader.setUniformVec4f("color", color)
        shader.setUniformVec4f("borderColor", borderColor)
        shader.setUniformFloat("borderWidth", borderWidth)
        atlas.texture.bind()
        val vao = vaoContext.getVAO(this) { vao ->
            vao.bind()
            vbo.bind()

            vao.attrib(0, 2, GL45.GL_FLOAT, false, 4 * Float.SIZE_BYTES, 0)
            vao.attrib(1, 2, GL45.GL_FLOAT, false, 4 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

            vao.enableAttrib(0)
            vao.enableAttrib(1)

            ibo.bind()
        }
        vao.bind()
        GL45.glDrawElements(GL45.GL_TRIANGLES, indices.size, GL45.GL_UNSIGNED_INT, 0)
    }

    override fun destroy() {
        TODO("Not yet implemented")
    }

    fun initialized(): Boolean {
        return initialized
    }
}