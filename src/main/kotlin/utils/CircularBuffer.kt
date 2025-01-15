package utils

class CircularBuffer<T>(val capacity: Int) : Iterable<T> {
    private var buffer: Array<Any?> = arrayOfNulls(capacity)
    private var index = 0
    var size = 0
        private set

    fun add(element: T) {
        buffer[index] = element
        index = (index + 1) % capacity

        size = minOf(capacity, size + 1)
    }

    fun clear() {
        buffer = arrayOfNulls(capacity)
        index = 0
        size = 0
    }

    fun getList(): List<T> {
        val list = mutableListOf<T>()

        for(i in index..<index+capacity) {
            if(buffer[i % capacity] == null) continue
            list.add(buffer[i % capacity] as T)
        }

        return list
    }

    fun get(i: Int): T? {
        return buffer[index + i % capacity] as T?
    }

    override fun iterator(): Iterator<T> {
        return getList().iterator()
    }
}