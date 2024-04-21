package rendering
import org.lwjgl.opengl.GL45.*

class ShaderProgram {
    val id: Int
    var vsID: Int = 0
    var fsID: Int = 0

    init {
        id = glCreateProgram()

        if (id == 0) {
            throw Exception("Could not create shader program!")
        }
    }

    fun createVertexShader(shaderCode: String) {
        vsID = createShader(shaderCode, GL_VERTEX_SHADER)
    }

    fun createFragmentShader(shaderCode: String) {
        fsID = createShader(shaderCode, GL_FRAGMENT_SHADER)
    }

    fun createShader(shaderCode: String, type: Int): Int {
        val shaderID = glCreateShader(type)

        if(shaderID == 0) throw Exception("Error creating shader. Type: $type")

        glShaderSource(shaderID, shaderCode)
        glCompileShader(shaderID)

        if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) throw Exception("Error compiling shader code: ${glGetShaderInfoLog(shaderID)}");

        glAttachShader(id, shaderID)
        return shaderID
    }

    fun link() {
        glLinkProgram(id)
        if(glGetProgrami(id, GL_LINK_STATUS) == 0) throw Exception("Error linking shader program $id: ${glGetProgramInfoLog(id)}")

        if(vsID != 0) glDeleteShader(vsID)
        if(fsID != 0) glDeleteShader(fsID)

        glValidateProgram(id)

        if(glGetProgrami(id, GL_VALIDATE_STATUS) == 0) error("Warning validating shader code ${glGetProgramInfoLog(id)}")
    }

    fun bind() {
        glUseProgram(id)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun destroy() {
        unbind()
        if (id != 0) glDeleteProgram(id)
    }
}