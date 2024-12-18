package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.FloatBuffer
import java.nio.IntBuffer

class GLBufferObject(private val target: Int = GL_ARRAY_BUFFER) {
    val id: Int = glGenBuffers()

    fun bind() {
        glBindBuffer(target, id)
    }

    fun uploadData(data: FloatBuffer, usage: Int) {
        glBufferData(target, data, usage)
    }

    fun uploadData(data: IntBuffer, usage: Int) {
        glBufferData(target, data, usage)
    }

    fun uploadSubData(data: FloatBuffer, offset: Long) {
        glBufferSubData(target, offset, data)
    }

    fun destroy() {
        glDeleteBuffers(id)
    }
}