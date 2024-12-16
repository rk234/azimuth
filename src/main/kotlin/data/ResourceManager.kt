package data

interface ResourceManager<T> {
    fun getResource(name: String): T
    fun setResource(name: String): T
}
