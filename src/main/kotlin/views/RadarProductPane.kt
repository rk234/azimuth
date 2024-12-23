package views

import data.resources.GeoJSONManager
import meteo.radar.RadarProductVolume
import map.MapView
import map.layers.GeoJSONLayer
import map.layers.RadarLayer
import meteo.radar.Product
import meteo.radar.RadarVolume
import org.joml.Vector3f
import org.json.JSONObject
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import kotlin.math.roundToInt

class RadarProductPane(var volume: RadarVolume, var product: Product, var tilt: Int) : JPanel() {
    var map: MapView = MapView()

    init {
        val countries = GeoJSONManager.instance.countries
        val counties = GeoJSONManager.instance.counties
        val states = GeoJSONManager.instance.states

        val productVolume = volume.getProductVolume(product)
        map.addLayer(RadarLayer(productVolume!!, tilt))
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
        val productSelect = JComboBox<String>()
        for (product in Product.entries) {
            productSelect.addItem(product.displayName)
        }
        productSelect.selectedItem = product.displayName
        productSelect.alignmentX = JLabel.LEFT_ALIGNMENT
//        productLbl.putClientProperty("FlatLaf.styleClass", "h3")
//        productSelect.putClientProperty("FlatLaf.style", "font: bold \$h3.regular.font");
        productSelect.maximumSize = Dimension(300, 50)

        topRow.add(productSelect)
        topRow.add(Box.createHorizontalGlue())
        header.add(topRow)

        val row = JPanel()
        row.isOpaque = false
        row.layout = BoxLayout(row, BoxLayout.X_AXIS)

        val tiltLbl = JLabel("Tilt: %.2f deg".format(productVolume.scans[tilt].elevation))
        tiltLbl.alignmentX = JLabel.LEFT_ALIGNMENT

        val dateTime = ZonedDateTime.ofInstant(ZonedDateTime.parse(volume.timeCoverageEnd).toInstant(), ZoneOffset.UTC)
        val localTime = dateTime.withZoneSameInstant(ZoneId.systemDefault())

        val timeLbl = JLabel(formatDateTime(localTime))
        timeLbl.alignmentX = JLabel.RIGHT_ALIGNMENT

        row.add(tiltLbl)
        row.add(Box.createHorizontalGlue())
        row.add(timeLbl)

        header.add(Box.createVerticalStrut(4))
        header.add(row)
        val cmapBar = ColormapBar(product.colormap, 250)
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
}