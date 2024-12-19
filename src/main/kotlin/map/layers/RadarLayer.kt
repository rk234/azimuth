package map.layers

import data.ShaderManager
import meteo.radar.RadarProductVolume
import map.projection.MercatorProjection
import map.projection.aerToGeo
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File
import java.nio.IntBuffer

class RadarLayer(private val volume: RadarProductVolume, private val tilt: Int) : MapLayer {
    private lateinit var radarShader: ShaderProgram
    private lateinit var cmapTexture: Texture1D
    private lateinit var radarRenderable: RadarRenderable

    override fun init(camera: Camera) {
        radarShader = ShaderManager.instance.radarShader()

        val cmap = volume.product.colormap
        val colormapImageData = MemoryUtil.memAlloc(100 * 3)
        cmap.genTextureData(100, colormapImageData)
        println("Image Data: ${colormapImageData.get(0)}")
        cmapTexture = Texture1D()
        cmapTexture.bind()
        cmapTexture.uploadData(100, colormapImageData.flip())
        MemoryUtil.memFree(colormapImageData)

        radarRenderable = RadarRenderable(volume, tilt, radarShader, cmapTexture)
        radarRenderable.init()

        val proj = MercatorProjection()
        val camPos = proj.toCartesian(Vector2f(volume.latitude, volume.longitude))
        camera.position = Vector3f(camPos.x, camPos.y, 0f)
        camera.zoom = 0.001f
        camera.recalcProjection()
        camera.recalcTransform()
    }



    override fun render(camera: Camera) {
        radarRenderable.draw(camera)
    }

    override fun destroy() {
        radarRenderable.destroy()
        radarShader.destroy()
        cmapTexture.destroy()
    }
}