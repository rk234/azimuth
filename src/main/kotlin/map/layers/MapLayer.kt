package map.layers

import rendering.Camera

interface MapLayer {
    fun init(camera: Camera)
    fun render(camera: Camera)
    fun destroy()
}