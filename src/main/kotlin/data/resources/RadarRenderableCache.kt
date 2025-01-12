package data.resources

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import meteo.radar.RadarSweep
import rendering.RadarSweepRenderable
import utils.invokeLaterOnRenderThread

class RadarRenderableCache(var maxCacheSize: Int) {
    companion object {
        val instance = RadarRenderableCache(20)
    }

    private var cacheMutex = Mutex()
    private var cache = LinkedHashMap<String, RadarSweepRenderable>()

    suspend fun put(radarSweep: RadarSweep, renderable: RadarSweepRenderable) {
        cacheMutex.withLock {
            cache[sweepKey(radarSweep)] = renderable
        }

        while (cacheSize() > maxCacheSize) {
            val toRemove: String
            cacheMutex.withLock {
                toRemove = cache.firstEntry().key
            }
            remove(toRemove)
        }
    }

    suspend fun cacheSize(): Int {
        cacheMutex.withLock {
            return cache.size
        }
    }

    suspend fun get(radarSweep: RadarSweep): RadarSweepRenderable {
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

            put(radarSweep, renderable)
            renderable
        }
    }

    suspend fun remove(key: String) {
        val renderable = cache[key]
        if (renderable != null) {
            cacheMutex.withLock {
                println("cache size: ${cache.size}")
                println("cache keys: ${cache.keys}")
                cache.remove(key)
                invokeLaterOnRenderThread {
                    println("Destroying renderable ${key}...")
                    renderable.destroy()
                }
            }
        }
    }

    suspend fun remove(radarSweep: RadarSweep) {
        remove(sweepKey(radarSweep))
    }

    suspend fun clear() {
        for (key in cache.keys) {
            remove(key)
        }
    }

    private fun sweepKey(sweep: RadarSweep): String {
        return sweep.fileHandle.toString() + "_" + sweep.product.dataField + "_" +
                sweep.tiltIndex
    }
}