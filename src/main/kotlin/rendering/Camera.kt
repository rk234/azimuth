package rendering

import org.joml.Matrix4f

class Camera {
    //var projMatrix: Matrix4f
    var transformMatrix: Matrix4f

    init {
        transformMatrix = Matrix4f()
    }
}