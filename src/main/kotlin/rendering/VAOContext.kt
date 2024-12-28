package rendering

class VAOContext {
    private val mapping = mutableMapOf<Renderable, VertexArrayObject>()

    fun getVAO(renderable: Renderable): VertexArrayObject {
        if(mapping.containsKey(renderable)) {
            return mapping[renderable]!!
        } else {
            println("creating for ${renderable}")
            mapping[renderable] = VertexArrayObject()
            return mapping[renderable]!!
        }
    }
}