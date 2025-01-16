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

    private var putMutex = Mutex()
    private var getMutex = Mutex()
    private var removeMutex = Mutex()
    private var cache = LinkedHashMap<String, RadarSweepRenderable>()

    suspend fun put(radarSweep: RadarSweep, renderable: RadarSweepRenderable) {
        putMutex.withLock {
            println("Putting renderable ${sweepKey(radarSweep)} in cache...")
            cache[sweepKey(radarSweep)] = renderable

            while (cacheSize() > maxCacheSize) {
                val toRemove = cache.firstEntry().key
                remove(toRemove)
            }
        }
    }

    fun cacheSize(): Int {
        return cache.size
    }

    suspend fun get(radarSweep: RadarSweep): RadarSweepRenderable {
        getMutex.withLock {
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
    }

    private suspend fun remove(key: String) {
        removeMutex.withLock {
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
    }

    private suspend fun remove(radarSweep: RadarSweep) {
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