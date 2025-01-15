package data.state

class State<T>(value: T) {
    var value: T = value
        set(value) {
            field = value
            for(listener in listeners) {
                listener(value)
            }
        }

    private var listeners: MutableSet<(T) -> Unit> = mutableSetOf()

    fun onChange(listener: (T) -> Unit) {
        listeners.add(listener)
    }
}