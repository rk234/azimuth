package rendering
import com.sun.jna.Memory
import org.joml.Matrix4f
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class ShaderProgram {
    val id: Int
    var vsID: Int = 0
    var fsID: Int = 0

    val uniforms = HashMap<String, Int>()

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

    fun setUniformMatrix4f(uniformName: String, data: Matrix4f) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        val buf = MemoryUtil.memAllocFloat(16)
        glUniformMatrix4fv(loc, false, data.get(buf))
        MemoryUtil.memFree(buf)
    }

    fun setUniformInt(uniformName: String, data: Int) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        glUniform1i(loc, data)
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