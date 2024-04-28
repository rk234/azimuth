package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.FloatBuffer

class GLBufferObject {
    val id: Int

    init {
        id = glGenBuffers()
    }

    fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id)
    }

    fun uploadData(data: FloatBuffer, usage: Int) {
        glBufferData(GL_ARRAY_BUFFER, data, usage)
    }

    fun uploadSubData(data: FloatBuffer, offset: Long) {
        glBufferSubData(GL_ARRAY_BUFFER, offset, data)
    }

    fun destroy() {
        glDeleteBuffers(id)
    }
}