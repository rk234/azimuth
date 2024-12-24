package data.resources

import meteo.radar.RadarSweep
import rendering.RadarScanRenderable

class RadarRenderableCache(var cacheSize: Int) {
    companion object {
        val instance = RadarRenderableCache(20)
    }

    private var cache = LinkedHashMap<String, RadarScanRenderable>()

    fun put(radarSweep: RadarSweep, renderable: RadarScanRenderable) {
        cache[sweepKey(radarSweep)] = renderable
        if(cache.size > cacheSize) {
            cache.remove(cache.firstEntry().key)
        }

        println(cache.keys)
    }

    fun get(radarSweep: RadarSweep): RadarScanRenderable {
        return if(cache.containsKey(sweepKey(radarSweep))) {
            cache[sweepKey(radarSweep)]!!
        } else {
            val renderable =  RadarScanRenderable(
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

    fun clear() {
        cache.clear()
    }

    private fun sweepKey(sweep: RadarSweep): String {
        return sweep.fileHandle.toString() + "_" + sweep.product.dataField + "_" +
                sweep.tiltIndex
    }
}