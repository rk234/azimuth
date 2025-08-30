package utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue

object RenderThreadTaskQueue {
    private val queue = ConcurrentLinkedQueue<Runnable>()
    private val mutex = Mutex()

    suspend fun offer(runnable: Runnable) {
        mutex.withLock {
            queue.offer(runnable)
        }
    }

    suspend fun poll(): Runnable? {
        mutex.withLock {
            return queue.poll()
        }
    }

    suspend fun isEmpty() = mutex.withLock {queue.isEmpty()}
}

suspend fun invokeLaterOnRenderThread(runnable: Runnable) {
    RenderThreadTaskQueue.offer(runnable)
}

fun tripleToVec3f(triple: Triple<Float, Float, Float>) = org.joml.Vector3f(triple.first, triple.second, triple.third)