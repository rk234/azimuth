package rendering
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class ShaderProgram {
    private val id: Int = glCreateProgram()
    private var vsID: Int = 0
    private var fsID: Int = 0

    private val uniforms = HashMap<String, Int>()
    private val matrix4fBuf = MemoryUtil.memAllocFloat(16)

    init {
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
        glUniformMatrix4fv(loc, false, data.get(matrix4fBuf))
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

    fun setUniformFloat(uniformName: String, data: Float) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        glUniform1f(loc, data)
    }

    fun setUniformVec2f(uniformName: String, data: Vector2f) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        glUniform2f(loc, data.x, data.y)
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
        MemoryUtil.memFree(matrix4fBuf)
    }

    fun setUniformVec3f(uniformName: String, value: Vector3f) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        glUniform3f(loc, value.x, value.y, value.z)
    }
    fun setUniformVec4f(uniformName: String, value: Vector4f) {
        val loc: Int;
        if(uniforms.containsKey(uniformName)) {
            loc = uniforms[uniformName]!!
        } else {
            loc = glGetUniformLocation(id, uniformName)
            uniforms[uniformName] = loc
        }
        glUniform4f(loc, value.x, value.y, value.z, value.w)
    }

}