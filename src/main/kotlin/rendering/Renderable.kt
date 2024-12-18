package rendering

interface Renderable {
    fun init()
    fun draw(camera: Camera)
    fun destroy()
}