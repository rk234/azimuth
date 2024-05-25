package rendering

import org.lwjgl.opengl.GL45.*
import java.nio.ByteBuffer

class Texture1D {
    val id: Int

    init {
        id = glGenTextures()
    }

    fun bind() {
        glBindTexture(GL_TEXTURE_1D, id)
    }

    fun uploadData(width: Int, imgData: ByteBuffer) {
        glTexImage1D(GL_TEXTURE_1D, 0, GL_RGB, width, 0, GL_RGB, GL_UNSIGNED_BYTE, imgData)
        glGenerateMipmap(GL_TEXTURE_1D)
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    fun destroy() {
        glDeleteTextures(id)
    }
}