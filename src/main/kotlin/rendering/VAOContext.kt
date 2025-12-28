package rendering

class VAOContext {
    private val mapping = mutableMapOf<Renderable, VertexArrayObject>()

    fun getVAO(renderable: Renderable, onCreate: ((vao: VertexArrayObject) -> Unit)? = null): VertexArrayObject {
        if(mapping.containsKey(renderable)) {
            return mapping[renderable]!!
        } else {
//            println("creating for ${renderable}")
            mapping[renderable] = VertexArrayObject()
            if (onCreate != null) {
                onCreate(mapping[renderable]!!)
            }
            return mapping[renderable]!!
        }
    }
    
    // Add dispose method to clean up all VAOs
    fun dispose() {
        mapping.values.forEach { it.destroy() }
        mapping.clear()
    }
}