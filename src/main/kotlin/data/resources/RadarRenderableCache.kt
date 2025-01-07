package data.resources

import kotlinx.coroutines.sync.Mutex
import meteo.radar.RadarSweep
import rendering.RadarScanRenderable
import utils.invokeLaterOnRenderThread

class RadarRenderableCache(var cacheSize: Int) {
    companion object {
        val instance = RadarRenderableCache(20)
    }

    private var cacheMutex = Mutex()
    private var cache = LinkedHashMap<String, RadarScanRenderable>()

    suspend fun put(radarSweep: RadarSweep, renderable: RadarScanRenderable) {
        cacheMutex.lock()
        cache[sweepKey(radarSweep)] = renderable

        if(cache.size > cacheSize) {
            remove(cache.firstEntry().key)
        }
        cacheMutex.unlock()

        println("RENDERABLE CACHE: ${cache.keys}")
    }

    suspend fun get(radarSweep: RadarSweep): RadarScanRenderable {
//        println("Attempting to get ${sweepKey(radarSweep)}")
        return if(cache.containsKey(sweepKey(radarSweep))) {
            cache[sweepKey(radarSweep)]!!
        } else {
            val renderable = RadarScanRenderable(
                radarSweep,
                ShaderManager.instance.radarShader(),
                ColormapTextureManager.instance.get(
                    ColormapManager.instance.getDefault(radarSweep.product)
                )!!
            )

            put(radarSweep, renderable)

            return renderable
        }
    }

    suspend fun remove(key: String) {
        cacheMutex.lock()
        val renderable = cache[key]
        if(renderable != null) {
            cache.remove(key)
            invokeLaterOnRenderThread {
                println("Destroying renderable ${key}...")
                renderable.destroy()
            }
        }
        cacheMutex.unlock()
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