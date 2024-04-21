package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.FloatBuffer

class VertexBufferObject {
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

    fun destroy() {
        glDeleteBuffers(id)
    }
}