package data.resources

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import meteo.radar.RadarSweep
import rendering.RadarSweepRenderable
import utils.invokeLaterOnRenderThread

class RadarRenderableCache(private var maxCacheSize: Int) {
    companion object {
        val instance = RadarRenderableCache(20)
    }
    private var cache = LinkedHashMap<String, RadarSweepRenderable>()
    private var cacheMutex = Mutex()

    suspend fun put(radarSweep: RadarSweep, renderable: RadarSweepRenderable) {
        cacheMutex.withLock {
            putNoLock(radarSweep, renderable)
        }
    }

    private suspend fun putNoLock(radarSweep: RadarSweep, renderable: RadarSweepRenderable) {
        println("Putting renderable ${sweepKey(radarSweep)} in cache...")
        cache[sweepKey(radarSweep)] = renderable

        while (cacheSize() > maxCacheSize) {
            val toRemove = cache.firstEntry().key
            removeNoLock(toRemove)
        }
    }

    fun cacheSize(): Int {
        return cache.size
    }

    suspend fun get(radarSweep: RadarSweep): RadarSweepRenderable {
        cacheMutex.withLock {
            return if (cache.containsKey(sweepKey(radarSweep))) {
                cache[sweepKey(radarSweep)]!!
            } else {
                val renderable = RadarSweepRenderable(
                    radarSweep,
                    ShaderManager.instance.radarShader(),
                    ColormapTextureManager.instance.get(
                        ColormapManager.instance.getDefault(radarSweep.product)
                    )!!
                )

                putNoLock(radarSweep, renderable)
                renderable
            }
        }
    }

    private suspend fun remove(key: String) {
        cacheMutex.withLock {
            removeNoLock(key)
        }
    }

    private suspend fun removeNoLock(key: String) {
        val renderable = cache[key]
        if (renderable != null) {
            println("cache size: ${cache.size}")
            println("cache keys: ${cache.keys}")
            cache.remove(key)
            invokeLaterOnRenderThread {
                println("Destroying renderable ${key}...")
                renderable.destroy()
            }
        }
    }

    suspend fun clear() {
        cacheMutex.withLock {
            for (key in cache.keys) {
                removeNoLock(key)
            }
        }
    }

    private fun sweepKey(sweep: RadarSweep): String {
        return sweep.fileHandle.toString() + "_" + sweep.product.dataField + "_" +
                sweep.tiltIndex
    }
}