package map

import rendering.Renderer

interface MapLayer {
    fun init()
    fun render(renderer: Renderer)
}