package data.state

class State<T>(value: T) {
    var value: T = value
        set(value) {
            for(listener in listeners) {
                listener(value)
            }
        }

    private var listeners: MutableList<(T) -> Unit> = mutableListOf()

    fun onChange(listener: (T) -> Unit) {
        listeners.add(listener)
    }
}