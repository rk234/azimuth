package rendering

import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import java.io.File
import java.nio.FloatBuffer
import java.nio.IntBuffer

class PathRenderable(
    private val vertices: List<Vector2f>,
    private val shader: ShaderProgram,
    private val lineWidth: Float,
    private val lineColor: Vector3f,
    private val zoomLevel: Float
) : Renderable {
    private lateinit var pathVBO: GLBufferObject
    private lateinit var prevVBO: GLBufferObject
    private lateinit var nextVBO: GLBufferObject
    private lateinit var dirVBO: GLBufferObject

    private lateinit var ibo: GLBufferObject
    private lateinit var vao: VertexArrayObject

    override fun init(vaoContext: VAOContext) {
        val verts = MemoryUtil.memAllocFloat((vertices.size * 2) * 2)
        val next = MemoryUtil.memAllocFloat((vertices.size * 2) * 2)
        val prev = MemoryUtil.memAllocFloat((vertices.size * 2) * 2)
        val dirs = MemoryUtil.memAllocFloat((vertices.size * 2))

        pack(duplicate(vertices), verts)
        pack(duplicate(shiftVerts(vertices, +1)), next)
        pack(duplicate(shiftVerts(vertices, -1)), prev)

        vertices.forEach { _ ->
            dirs.put(1.0f)
            dirs.put(-1.0f)
        }

        verts.flip()
        next.flip()
        prev.flip()
        dirs.flip()

        vao = vaoContext.getVAO(this)
        vao.bind()

        pathVBO = GLBufferObject()
        pathVBO.bind()
        pathVBO.uploadData(verts, GL_STATIC_DRAW)

        prevVBO = GLBufferObject()
        prevVBO.bind()
        prevVBO.uploadData(prev, GL_STATIC_DRAW)

        nextVBO = GLBufferObject()
        nextVBO.bind()
        nextVBO.uploadData(next, GL_STATIC_DRAW)

        dirVBO = GLBufferObject()
        dirVBO.bind()
        dirVBO.uploadData(dirs, GL_STATIC_DRAW)

        ibo = GLBufferObject(GL_ELEMENT_ARRAY_BUFFER)

        val indices = MemoryUtil.memAllocInt((vertices.size) * 6)
        generateIndices(indices, vertices.size)

        MemoryUtil.memFree(verts)
        MemoryUtil.memFree(prev)
        MemoryUtil.memFree(next)
        MemoryUtil.memFree(indices)

        pathVBO.bind()
        vao.attrib(0, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(0)
        nextVBO.bind()
        vao.attrib(1, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(1)
        prevVBO.bind()
        vao.attrib(2, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(2)
        dirVBO.bind()
        vao.attrib(3, 1, GL_FLOAT, false, 1 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(3)
        ibo.bind()
        ibo.uploadData(indices, GL_STATIC_DRAW)
    }

    override fun draw(camera: Camera, vaoContext: VAOContext) {
        shader.bind()
        shader.setUniformMatrix4f("projection", camera.projectionMatrix)
        shader.setUniformMatrix4f("transform", camera.transformMatrix)
        shader.setUniformFloat("aspect", camera.viewportDims.y / camera.viewportDims.x)
        shader.setUniformVec2f("resolution", camera.viewportDims)
        shader.setUniformFloat("thickness", lineWidth)
        shader.setUniformInt("miter", 0)
        if(camera.zoom > zoomLevel) {
            shader.setUniformVec4f("color", Vector4f(lineColor.x, lineColor.y, lineColor.z, 1.0f))
        } else {
            shader.setUniformVec4f("color", Vector4f(lineColor.x, lineColor.y, lineColor.z, camera.zoom / zoomLevel))
        }

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        vao.bind()

        glDrawElements(GL_TRIANGLES, vertices.size * 6, GL_UNSIGNED_INT, 0)
    }

    override fun destroy() {
        vao.destroy()
        pathVBO.destroy()
        dirVBO.destroy()
        prevVBO.destroy()
        nextVBO.destroy()
        ibo.destroy()
    }

    private fun pack(path: List<Vector2f>, buf: FloatBuffer) {
        for (vec in path) {
            buf.put(vec[0])
            buf.put(vec[1])
        }
    }

    private fun duplicate(path: List<Vector2f>): List<Vector2f> {
        val list = ArrayList<Vector2f>(path.size * 2)
        path.forEach { vert ->
            list.add(vert)
            list.add(vert)
        }
        return list
    }

    private fun shiftVerts(path: List<Vector2f>, offset: Int): List<Vector2f> {
        val list = ArrayList<Vector2f>(path.size)
        for (i in 0..<path.size) {
            list.add(path[clamp(i + offset, path.size - 1, 0)])
        }
        return list
    }

    private fun generateIndices(indices: IntBuffer, pathLen: Int) {
        var c = 0
        var index = 0
        for (j in 0..<pathLen) {
            val i = index
            indices.put(c++, i)
            indices.put(c++, i + 1)
            indices.put(c++, i + 2)
            indices.put(c++, i + 2)
            indices.put(c++, i + 1)
            indices.put(c++, i + 3)
            index += 4
        }
    }

    private fun clamp(v: Int, max: Int, min: Int): Int {
        return if (v > max) max else if (v < min) min else v;
    }
}