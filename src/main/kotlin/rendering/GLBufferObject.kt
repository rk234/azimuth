package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GLBufferObject {
    val id: Int

    init {
        id = glGenBuffers()
    }

    fun bind(target: Int = GL_ARRAY_BUFFER) {
        glBindBuffer(target, id)
    }

    fun uploadData(data: FloatBuffer, usage: Int, target: Int = GL_ARRAY_BUFFER) {
        glBufferData(target, data, usage)
    }

    fun uploadData(data: IntBuffer, usage: Int, target: Int = GL_ARRAY_BUFFER) {
        glBufferData(target, data, usage)
    }

    fun uploadSubData(data: FloatBuffer, offset: Long) {
        glBufferSubData(GL_ARRAY_BUFFER, offset, data)
    }

    fun destroy() {
        glDeleteBuffers(id)
    }
}