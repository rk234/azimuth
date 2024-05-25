package rendering

import org.lwjgl.opengl.GL45.*

class VertexArrayObject {
    val id: Int

    init {
        id = glGenVertexArrays()
    }

    fun bind() {
        glBindVertexArray(id)
    }

    fun enableAttrib(index: Int) {
        glEnableVertexAttribArray(index)
    }

    fun attrib(index: Int, size: Int, type: Int, norm: Boolean = false, stride: Int, offset: Long) {
        glVertexAttribPointer(index, size, type, norm, stride, offset)
    }

    fun destroy() {
        glDeleteVertexArrays(id)
    }
}