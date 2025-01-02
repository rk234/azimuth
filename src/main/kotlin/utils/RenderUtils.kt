package utils

import java.util.concurrent.LinkedBlockingQueue

object RenderThreadTaskQueue {
    private val queue = LinkedBlockingQueue<Runnable>()

    fun offer(runnable: Runnable) {
        queue.offer(runnable)
    }

    fun poll(): Runnable? {
        return queue.poll()
    }

    fun isEmpty() = queue.isEmpty()
}

fun invokeLaterOnRenderThread(runnable: Runnable) {
    RenderThreadTaskQueue.offer(runnable)
}