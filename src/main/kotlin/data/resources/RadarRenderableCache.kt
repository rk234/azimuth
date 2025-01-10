package data.resources

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import meteo.radar.RadarSweep
import rendering.RadarSweepRenderable
import utils.invokeLaterOnRenderThread

class RadarRenderableCache(var cacheSize: Int) {
    companion object {
        val instance = RadarRenderableCache(20)
    }

    private var cacheMutex = Mutex()
    private var cache = LinkedHashMap<String, RadarSweepRenderable>()

    suspend fun put(radarSweep: RadarSweep, renderable: RadarSweepRenderable) {
        cacheMutex.withLock {
            cache[sweepKey(radarSweep)] = renderable

            println("RENDERABLE CACHE: ${cache.keys}")
        }

        if(cache.size > cacheSize) {
            remove(cache.firstEntry().key)
        }
    }

    suspend fun get(radarSweep: RadarSweep): RadarSweepRenderable {
        println("Attempting to get ${sweepKey(radarSweep)}")
//        println("RENDERABLE CACHE: ${cache.keys}")
        return if(cache.containsKey(sweepKey(radarSweep))) {
            println("\t${sweepKey(radarSweep)} found in cache")
            cache[sweepKey(radarSweep)]!!
        } else {
            println("\t${sweepKey(radarSweep)} not found in cache, generating")
            val renderable = RadarSweepRenderable(
                radarSweep,
                ShaderManager.instance.radarShader(),
                ColormapTextureManager.instance.get(
                    ColormapManager.instance.getDefault(radarSweep.product)
                )!!
            )

            put(radarSweep, renderable)
            println("HERE: ${sweepKey(radarSweep)}")
            renderable
        }
    }

    suspend fun remove(key: String) {
        cacheMutex.withLock {
            val renderable = cache[key]
            if(renderable != null) {
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
        for(key in cache.keys) {
            remove(key)
        }
    }

    private fun sweepKey(sweep: RadarSweep): String {
        return sweep.fileHandle.toString() + "_" + sweep.product.dataField + "_" +
                sweep.tiltIndex
    }
}