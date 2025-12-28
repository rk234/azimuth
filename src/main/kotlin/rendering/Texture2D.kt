package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.ByteBuffer

class Texture2D {
    val id: Int

    init {
        id = glGenTextures()
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_2D, id)
    }

    fun uploadData(width: Int, height: Int, imgData: ByteBuffer) {
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, width, height, 0, GL_RED, GL_UNSIGNED_BYTE, imgData)
    }

    fun setParameters(minFilter: Int, magFilter: Int, wrapS: Int, wrapT: Int) {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minFilter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magFilter)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrapS)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrapT)
    }

    fun destroy() {
        glDeleteTextures(id)
    }
}

