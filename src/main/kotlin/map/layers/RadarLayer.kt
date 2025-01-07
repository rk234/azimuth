package map.layers

import data.resources.ColormapManager
import data.resources.ColormapTextureManager
import data.resources.RadarRenderableCache
import data.resources.ShaderManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.runBlocking
import meteo.radar.RadarProductVolume
import map.projection.MercatorProjection
import meteo.radar.RadarVolume
import org.joml.Vector2f
import org.joml.Vector3f
import rendering.*

class RadarLayer(private var volume: RadarProductVolume, private var tilt: Int) : MapLayer {
    private lateinit var radarRenderable: RadarScanRenderable
    private var initialized = false

    suspend fun setProductVolumeAndTilt(volume: RadarProductVolume, tilt: Int) {
        this.volume = volume
        this.tilt = tilt

        val newRenderable = RadarRenderableCache.instance.get(this.volume.scans[tilt])
        if(!newRenderable.hasGeometry)
            newRenderable.createGeometry()

        radarRenderable = newRenderable
    }

    override fun init(camera: Camera, vaoContext: VAOContext) {
        runBlocking {
            radarRenderable = RadarRenderableCache.instance.get(volume.scans[tilt]) ?: throw Exception("failed to generate renderable")
        }

        if(!radarRenderable.initialized())
            radarRenderable.init(vaoContext)

        initialized = true;
    }

    override fun render(camera: Camera, vaoContext: VAOContext) {
        if(radarRenderable.hasGeometry && !radarRenderable.initialized())
            radarRenderable.init(vaoContext)

        if(radarRenderable.initialized())
            radarRenderable.draw(camera, vaoContext)
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