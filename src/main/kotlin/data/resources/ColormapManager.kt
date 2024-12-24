package data.resources

import meteo.radar.Colormap
import meteo.radar.Product
import java.io.File

class ColormapManager {
    companion object {
        val instance = ColormapManager()

        fun init() {
            loadDefaultColormaps()
        }

        private fun loadDefaultColormaps() {
            for(product in Product.entries) {
                instance.put("def_${product.dataField}", loadDefaultColormap(product))
            }
        }

        private fun loadDefaultColormap(product: Product): Colormap {
            return when(product) {
                Product.REFLECTIVITY_HIRES, Product.REFLECTIVITY -> {
                    Colormap("default_ref", File("src/main/resources/colormaps/reflectivity.cmap").readText())
                }
                Product.RADIAL_VEL_HIRES -> {
                    Colormap("default_vel", File("src/main/resources/colormaps/velocity.cmap").readText())
                }
                Product.CORRELATION_COEF_HIRES -> {
                    Colormap("default_cc", File("src/main/resources/colormaps/crosscorrelation.cmap").readText())
                }
            }
        }
    }

    val colormaps = mutableMapOf<String, Colormap>()

    fun put(name: String, colormap: Colormap) {
        colormaps[name] = colormap
    }

    fun get(name: String): Colormap? {
        return colormaps[name]
    }

    fun getDefault(product: Product): Colormap {
        return colormaps["def_${product.dataField}"]!!
    }
}