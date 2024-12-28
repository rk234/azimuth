package rendering

interface Renderable {
    fun init(vaoContext: VAOContext)
    fun draw(camera: Camera, vaoContext: VAOContext)
    fun destroy()
}