package views

import data.resources.GeoJSONManager
import data.state.AppState
import map.layers.GeoJSONLayer
import meteo.radar.Product
import org.joml.Vector3f
import org.lwjgl.opengl.awt.GLData
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

class RadarMultiPane : JPanel() {
    private val productPanes: Array<RadarProductPane?> = arrayOfNulls(4)

    private val numPanes = 4
    init {
        layout = GridLayout(2,2)

        val countries = GeoJSONManager.instance.countries
        val counties = GeoJSONManager.instance.counties
        val states = GeoJSONManager.instance.states

        val layers = arrayOf(
            GeoJSONLayer(countries, 0.05f, Vector3f(0.8f), -10f),
            GeoJSONLayer(counties, 0.03f, Vector3f(0.8f), 0.0001f),
            GeoJSONLayer(states, 0.035f, Vector3f(1.0f), -10f)
        )
        for(i in 0..<numPanes) {
            val glData = GLData()
            glData.majorVersion = 4
            glData.minorVersion = 6

            if(i > 0)
                glData.shareContext = productPanes[0]?.map

            productPanes[i] = RadarProductPane(AppState.activeVolume.value!!, Product.entries.getOrElse(i) {_ -> Product.REFLECTIVITY_HIRES}, 0, glData)
            for(layer in layers) productPanes[i]?.map?.addLayer(layer)

            add(productPanes[i])
        }
    }

    fun startRendering() {
        SwingUtilities.invokeLater {
            Timer(1000/60) {
                for(pane in productPanes) {
                    pane?.render()
                }
            }.start()
        }
    }
}