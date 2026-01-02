package views

import com.google.common.io.Files
import data.resources.GeoJSONManager
import data.state.AppState
import data.warnings.WarningType
import map.layers.GeoJSONLayer
import map.layers.Label
import map.layers.LabelLayer
import map.layers.WarningLayer
import map.projection.MercatorProjection
import meteo.radar.Product
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.awt.GLData
import utils.RenderThreadTaskQueue
import java.awt.GridLayout
import java.io.File
import javax.swing.JPanel
import javax.swing.Timer

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
    private val citiesCSV = Files.readLines(File("src/main/resources/geo/USCities.csv"), Charsets.UTF_8)


    private lateinit var renderTimer: Timer

    var fps: Int = 0

    private val layers = arrayOf(
        GeoJSONLayer(countries, 0.05f, Vector3f(0.8f), -10f),
        GeoJSONLayer(counties, 0.03f, Vector3f(0.8f), 0.0001f),
        GeoJSONLayer(states, 0.035f, Vector3f(1.0f), -10f),
        WarningLayer(AppState.warningDataManager, WarningType.TORNADO),
        WarningLayer(AppState.warningDataManager, WarningType.SEVERE_THUNDERSTORM),
        WarningLayer(AppState.warningDataManager, WarningType.FLASH_FLOOD),
        WarningLayer(AppState.warningDataManager, WarningType.SPECIAL_WEATHER_STATEMENT),
        WarningLayer(AppState.warningDataManager, WarningType.SPECIAL_MARINE),
        LabelLayer(generateCityLabels(citiesCSV))
    )

    private fun generateCityLabels(citiesCSV: List<String>): List<Label> {
        val labels = mutableListOf<Label>()
        val mercator = MercatorProjection()
        // Parse and store all labels
        for (line in citiesCSV) {
            val parts = line.split(",")
            val city = parts[0]
            val lat = parts[1].toFloat()
            val lon = parts[2].toFloat()
            val population = parts[3].toInt()
            val size = when {
                population > 1_000_000 -> 0.004f
                population > 500_000 -> 0.003f
                population > 100_000 -> 0.0025f
                else -> 0.002f
            }

            val coord = mercator.toCartesian(Vector2f(lat, lon))
            labels.add(Label(city, coord, population, size))
        }
        return labels
    }

    init {
        layout = createLayout(paneLayout)

        for(i in 0..<paneLayout.numPanes) {
            val glData = GLData()

            if(i > 0)
                glData.shareContext = productPanes[0]?.map

            val volume = AppState.activeVolume.value ?: throw Error("No active volume found!")
            productPanes[i] = createPane(volume
                .getSupportedProducts()
                .toList()
                .getOrElse(i) {_ -> Product.REFLECTIVITY_HIRES}
                , glData)

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

                val volume = AppState.activeVolume.value ?: throw Error("No active volume found!")

                productPanes[paneLayout.numPanes + i] =
                    createPane(volume.getSupportedProducts().toList().getOrElse(paneLayout.numPanes + i) { _ -> Product.REFLECTIVITY_HIRES }, glData)
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
        renderTimer = Timer(1000/60) {
            for (i in 0..<paneLayout.numPanes) {
                productPanes[i]?.render()
            }

            // Handle render thread tasks without blocking
            val task = RenderThreadTaskQueue.pollNonBlocking()
            task?.run()

            val dt = System.currentTimeMillis()-lastFrame
            lastFrame = System.currentTimeMillis()
            if(dt != 0L) {
                fps = Math.round(1000.0f / dt)
            }
        }
        renderTimer?.start()
    }

    fun stopRendering() {
        renderTimer.stop()
    }

    // Add cleanup method for proper resource disposal
    fun dispose() {
        stopRendering()
        for (i in productPanes.indices) {
            productPanes[i]?.dispose()
            productPanes[i] = null
        }
    }
}