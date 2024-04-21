package rendering

import org.lwjgl.opengl.GL45.*

class VertexArrayObject {
    val id: Int

    init {
        id = glGenVertexArrays()
    }

    //TODO: add vertex attribute methods
    
}