package data

interface PollableDataService<T> {
    fun init(numInitialData: Int = 0)
    fun poll(): T?
}