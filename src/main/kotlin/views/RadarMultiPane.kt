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
import kotlin.time.measureTime

enum class PaneLayout(val numPanes: Int) {
    SINGLE(1),
    DUAL(2),
    QUAD(4)
}

class RadarMultiPane(var paneLayout: PaneLayout) : JPanel() {
    private val productPanes: Array<RadarProductPane?> = arrayOfNulls(4)

    private val countries = GeoJSONManager.instance.countries
    private val counties = GeoJSONManager.instance.counties
    private val states = GeoJSONManager.instance.states

    var fps: Int = 0

    private val layers = arrayOf(
        GeoJSONLayer(countries, 0.05f, Vector3f(0.8f), -10f),
        GeoJSONLayer(counties, 0.03f, Vector3f(0.8f), 0.0001f),
        GeoJSONLayer(states, 0.035f, Vector3f(1.0f), -10f)
    )
    init {
        layout = createLayout(paneLayout)

        for(i in 0..<paneLayout.numPanes) {
            val glData = GLData()

            if(i > 0)
                glData.shareContext = productPanes[0]?.map

            productPanes[i] = createPane(Product.entries.getOrElse(i) {_ -> Product.REFLECTIVITY_HIRES}, glData)

            add(productPanes[i])
        }
    }

    fun setPaneLayout(newLayout: PaneLayout, horizontalSplit: Boolean = true) {
        if(newLayout.numPanes < paneLayout.numPanes) {
            val diff = paneLayout.numPanes - newLayout.numPanes
            for(i in 0..<diff) {
                remove(productPanes[paneLayout.numPanes-1-i])
            }
        } else {
            val diff = newLayout.numPanes - paneLayout.numPanes
            for(i in 0..<diff) {
                val glData = GLData()
                glData.shareContext = productPanes[0]?.map

                productPanes[paneLayout.numPanes + i] =
                    createPane(Product.entries.getOrElse(paneLayout.numPanes + i) { _ -> Product.REFLECTIVITY_HIRES }, glData)
                add(productPanes[paneLayout.numPanes + i])
            }
        }

        layout = createLayout(newLayout, horizontalSplit)
        revalidate()
        repaint()
        paneLayout = newLayout
    }

    private fun createPane(product: Product, glData: GLData): RadarProductPane {
        val pane =  RadarProductPane(AppState.activeVolume.value!!, product, 0, glData)
        for(layer in layers) pane.map.addLayer(layer)
        return pane
    }

    private fun createLayout(paneLayout: PaneLayout, horizontal: Boolean = true): GridLayout {
        return when (paneLayout) {
            PaneLayout.SINGLE, PaneLayout.DUAL -> {
                if(horizontal) GridLayout(1, 0) else GridLayout(0, 1)
            }
            PaneLayout.QUAD -> GridLayout(2,2)
        }
    }

    fun startRendering() {
        var lastFrame = System.currentTimeMillis()
        Timer(1000/60) {
            for (i in 0..<paneLayout.numPanes) {
                productPanes[i]?.render()
            }
            val dt = System.currentTimeMillis()-lastFrame
            lastFrame = System.currentTimeMillis()
            if(dt != 0L) {
                fps = Math.round(1000.0f / dt)
            }
        }.start()
    }
}