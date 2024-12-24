package data.resources

import meteo.radar.Colormap
import meteo.radar.Product
import org.lwjgl.system.MemoryUtil
import rendering.Texture1D
import java.awt.Color

class ColormapTextureManager {
    val colormapTextures = mutableMapOf<Colormap, Texture1D>()

    companion object {
        val colormapSampleCount = 100
        val instance = ColormapTextureManager()

        fun init() {
            loadDefaultColormaps()
        }

        private fun loadDefaultColormaps() {
            loadDefaultReflectivity()
            loadDefaultVelocity()
            loadDefaultCorrel()
        }

        private fun loadDefaultReflectivity() {
            val cmap = ColormapManager.instance.getDefault(Product.REFLECTIVITY_HIRES)
            val cmapTexture = createTexture(cmap)
            instance.put(cmap, cmapTexture)
        }

        private fun loadDefaultVelocity() {
            val cmap = ColormapManager.instance.getDefault(Product.RADIAL_VEL_HIRES)
            val cmapTexture = createTexture(cmap)
            instance.put(cmap, cmapTexture)
        }

        private fun loadDefaultCorrel() {
            val cmap = ColormapManager.instance.getDefault(Product.CORRELATION_COEF_HIRES)
            val cmapTexture = createTexture(cmap)
            instance.put(cmap, cmapTexture)
        }

        private fun createTexture(cmap: Colormap): Texture1D {
            val colormapImageData = MemoryUtil.memAlloc(colormapSampleCount * 3)
            cmap.genTextureData(colormapSampleCount, colormapImageData)

            val cmapTexture = Texture1D()
            cmapTexture.bind()
            cmapTexture.uploadData(colormapSampleCount, colormapImageData.flip())
            MemoryUtil.memFree(colormapImageData)
            return cmapTexture
        }
    }

    fun get(colormap: Colormap): Texture1D? {
        return colormapTextures[colormap]!!
    }

    fun put(colormap: Colormap, texture: Texture1D) {
        colormapTextures[colormap] = texture
    }
}