package map.layers

import rendering.Camera
import rendering.Renderer

interface MapLayer {
    fun init(camera: Camera)
    fun render(renderer: Renderer)
    fun destroy()
}