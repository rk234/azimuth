package map.layers

import rendering.Camera
import rendering.VAOContext

interface MapLayer {
    fun init(camera: Camera, vaoContext: VAOContext)
    fun render(camera: Camera, vaoContext: VAOContext)
    fun destroy()
    fun initialized(): Boolean
}