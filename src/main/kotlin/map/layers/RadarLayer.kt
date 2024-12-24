package map.layers

import data.resources.ColormapManager
import data.resources.ColormapTextureManager
import data.resources.RadarRenderableCache
import data.resources.ShaderManager
import meteo.radar.RadarProductVolume
import map.projection.MercatorProjection
import meteo.radar.RadarVolume
import org.joml.Vector2f
import org.joml.Vector3f
import rendering.*

class RadarLayer(private var volume: RadarProductVolume, private var tilt: Int) : MapLayer {
    private lateinit var radarRenderable: RadarScanRenderable
    private var initialized = false

    override fun init(camera: Camera) {
        radarRenderable = RadarRenderableCache.instance.get(volume.scans[tilt]) ?: throw Exception("failed to generate renderable")
        if(!radarRenderable.initialized())
            radarRenderable.init()

        initialized = true;
    }

    override fun render(camera: Camera) {
        radarRenderable.draw(camera)
    }

    override fun destroy() {
//        radarRenderable.destroy()
//        radarShader.destroy()
//        cmapTexture.destroy()
    }

    override fun initialized(): Boolean {
        return initialized
    }
}