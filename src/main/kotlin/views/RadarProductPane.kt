package views

import data.resources.ColormapManager
import data.resources.GeoJSONManager
import data.state.AppState
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import map.MapView
import map.layers.GeoJSONLayer
import map.layers.RadarLayer
import map.projection.MercatorProjection
import meteo.radar.Product
import meteo.radar.RadarVolume
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.awt.GLData
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture.runAsync
import javax.swing.*

class RadarProductPane(var volume: RadarVolume, var product: Product, var tilt: Int, glData: GLData? = null) : JPanel() {
    val map: MapView = MapView(glData)
    private var radarLayer: RadarLayer = RadarLayer(volume.getProductVolume(product)!!, tilt)

    private val scope: CoroutineScope = MainScope()

    private val productSelect = JComboBox<Product>()
    private var cmapBar: ColormapBar
    private var tiltLabel: JLabel
    private var timeLabel: JLabel
    private var volChangeJob: Job? = null

    init {
        AppState.activeVolume.onChange { vol ->
            runBlocking {
                volChangeJob?.cancelAndJoin()
            }
            volChangeJob = scope.launch(Dispatchers.IO) {
                handleVolumeChange(vol)
            }
        }

//        val countries = GeoJSONManager.instance.countries
//        val counties = GeoJSONManager.instance.counties
//        val states = GeoJSONManager.instance.states
//
        val proj = MercatorProjection()
        val camPos = proj.toCartesian(Vector2f(volume.station.latitude, volume.station.longitude))
        map.camera.position = Vector3f(camPos.x, camPos.y, 0f)
        map.camera.zoom = 0.0001f
        map.camera.recalcProjection()
        map.camera.recalcTransform()

        map.addLayer(radarLayer)
//        map.addLayer(GeoJSONLayer(countries, 0.05f, Vector3f(0.8f), -10f))
//        map.addLayer(GeoJSONLayer(counties, 0.03f, Vector3f(0.8f), 0.0001f))
//        map.addLayer(GeoJSONLayer(states, 0.035f, Vector3f(1.0f), -10f))

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        minimumSize = Dimension(100, 100)

        val header = JPanel()
        header.background = Color(50, 50, 50)
        header.layout = BoxLayout(header, BoxLayout.Y_AXIS)
        header.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val topRow = JPanel()
        topRow.isOpaque = false
        topRow.layout = BoxLayout(topRow, BoxLayout.X_AXIS)

        for (product in volume.getSupportedProducts()) {
            productSelect.addItem(product)
        }

        productSelect.selectedItem = product
        productSelect.alignmentX = JLabel.LEFT_ALIGNMENT
        productSelect.addActionListener {
            GlobalScope.launch(Dispatchers.Default) {
                handleProductChange(it)
            }
        }

//        productLbl.putClientProperty("FlatLaf.styleClass", "h3")
//        productSelect.putClientProperty("FlatLaf.style", "font: bold \$h3.regular.font");
        productSelect.maximumSize = Dimension(300, 50)

        topRow.add(productSelect)
        topRow.add(Box.createHorizontalGlue())
        header.add(topRow)

        val row = JPanel()
        row.isOpaque = false
        row.layout = BoxLayout(row, BoxLayout.X_AXIS)

        tiltLabel = JLabel()
        updateTiltLabel()
        tiltLabel.alignmentX = JLabel.LEFT_ALIGNMENT

        timeLabel = JLabel()
        updateTimeLabel()
        timeLabel.alignmentX = JLabel.RIGHT_ALIGNMENT

        row.add(tiltLabel)
        row.add(Box.createHorizontalGlue())
        row.add(timeLabel)

        header.add(Box.createVerticalStrut(4))
        header.add(row)
        cmapBar = ColormapBar(ColormapManager.instance.getDefault(product), 250)
        cmapBar.maximumSize = Dimension(Int.MAX_VALUE, 40)
        add(header)
        add(cmapBar)
        add(map)
    }

    private fun formatDateTime(dateTime: ZonedDateTime): String {
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d hh:mm:ss a")
        return fmt.format(dateTime)
    }

    fun render() {
        map.render()
    }

    private suspend fun handleProductChange(e: ActionEvent) = coroutineScope {
        launch(Dispatchers.Swing) {
            val selectedProduct = productSelect.selectedItem as Product
            product = selectedProduct
            radarLayer.setProductVolumeAndTilt(
                withContext(Dispatchers.IO) {
                    volume.getProductVolume(product)!!
                },
                tilt
            )
            cmapBar.setColormap(ColormapManager.instance.getDefault(product))
            cmapBar.repaint()
            updateTiltLabel()
            updateTimeLabel()
        }
    }

    private suspend fun handleVolumeChange(volume: RadarVolume?) {
        if(volume == null) return
//        println("${product.displayName} PANEL => volume change: ${volume.handle.fileName}")
        this.volume = volume
        radarLayer.setProductVolumeAndTilt(volume.getProductVolume(product)!!, tilt)
        updateTiltLabel()
        updateTimeLabel()
//        println("Volume updated!!")
    }

    private fun updateTimeLabel() {
        val dateTime = ZonedDateTime.ofInstant(ZonedDateTime.parse(volume.timeCoverageEnd).toInstant(), ZoneOffset.UTC)
        val localTime = dateTime.withZoneSameInstant(ZoneId.systemDefault())
        timeLabel.text = formatDateTime(localTime)
    }

    private fun updateTiltLabel() {
        tiltLabel.text = ("Tilt: %.2f deg".format(volume.getProductVolume(product)!!.scans[tilt].elevation))
    }
}