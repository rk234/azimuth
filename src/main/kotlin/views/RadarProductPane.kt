package views

import data.resources.ColormapManager
import data.resources.GeoJSONManager
import data.state.AppState
import map.MapView
import map.layers.GeoJSONLayer
import map.layers.RadarLayer
import map.projection.MercatorProjection
import meteo.radar.Product
import meteo.radar.RadarVolume
import org.joml.Vector2f
import org.joml.Vector3f
import java.awt.Color
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*

class RadarProductPane(var volume: RadarVolume, var product: Product, var tilt: Int) : JPanel() {
    private var map: MapView = MapView()
    private var productVolume = volume.getProductVolume(product)
    private var radarLayer: RadarLayer = RadarLayer(productVolume!!, tilt)

    private val productSelect = JComboBox<Product>()
    private var cmapBar: ColormapBar
    private var tiltLabel: JLabel
    private var timeLabel: JLabel


    init {
        AppState.activeVolume.onChange(::handleVolumeChange)

        val countries = GeoJSONManager.instance.countries
        val counties = GeoJSONManager.instance.counties
        val states = GeoJSONManager.instance.states

        val proj = MercatorProjection()
        val camPos = proj.toCartesian(Vector2f(volume.station.latitude, volume.station.longitude))
        map.camera.position = Vector3f(camPos.x, camPos.y, 0f)
        map.camera.zoom = 0.0001f
        map.camera.recalcProjection()
        map.camera.recalcTransform()

        map.addLayer(radarLayer)
        map.addLayer(GeoJSONLayer(countries, 0.05f, Vector3f(0.8f), -10f))
        map.addLayer(GeoJSONLayer(counties, 0.03f, Vector3f(0.8f), 0.0001f))
        map.addLayer(GeoJSONLayer(states, 0.035f, Vector3f(1.0f), -10f))

        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val header = JPanel()
        header.background = Color(50, 50, 50)
        header.layout = BoxLayout(header, BoxLayout.Y_AXIS)
        header.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val topRow = JPanel()
        topRow.isOpaque = false
        topRow.layout = BoxLayout(topRow, BoxLayout.X_AXIS)

        for (product in Product.entries) {
            productSelect.addItem(product)
        }

        productSelect.selectedItem = product.displayName
        productSelect.alignmentX = JLabel.LEFT_ALIGNMENT
        productSelect.addActionListener(::handleProductChange)

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
        cmapBar.maximumSize = Dimension(Int.MAX_VALUE, 25)
        add(cmapBar)
        add(header)
        add(map)
    }

    private fun formatDateTime(dateTime: ZonedDateTime): String {
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d hh:mm:ss a")
        return fmt.format(dateTime)
    }

    fun render() {
        map.render()
    }

    fun handleProductChange(e: ActionEvent) {
        val selectedProduct = productSelect.selectedItem as Product
        map.removeLayer(radarLayer)
        radarLayer = RadarLayer(volume.getProductVolume(selectedProduct)!!, tilt)
        map.insertLayer(0, radarLayer)
        cmapBar.setColormap(ColormapManager.instance.getDefault(selectedProduct))
        cmapBar.repaint()
    }

    fun handleVolumeChange(volume: RadarVolume?) {
        if(volume == null) return

        this.volume = volume
        map.removeLayer(radarLayer)
        radarLayer = RadarLayer(volume.getProductVolume(productSelect.selectedItem as Product)!!, tilt)
        map.insertLayer(0, radarLayer)
        updateTiltLabel()
        updateTimeLabel()
        println("Volume updated!!")
    }

    private fun updateTimeLabel() {
        val dateTime = ZonedDateTime.ofInstant(ZonedDateTime.parse(volume.timeCoverageEnd).toInstant(), ZoneOffset.UTC)
        val localTime = dateTime.withZoneSameInstant(ZoneId.systemDefault())
        timeLabel.text = formatDateTime(localTime)
    }

    private fun updateTiltLabel() {
        tiltLabel.text = ("Tilt: %.2f deg".format(productVolume!!.scans[tilt].elevation))
    }
}