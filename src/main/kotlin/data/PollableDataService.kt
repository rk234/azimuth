package data

interface PollableDataService<T> {
    fun init()
    fun poll(): T?
}