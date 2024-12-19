package views

import meteo.radar.RadarProductVolume
import map.MapView
import map.layers.GeoJSONLayer
import map.layers.RadarLayer
import meteo.radar.Product
import org.joml.Vector3f
import org.json.JSONObject
import java.awt.Color
import java.awt.Dimension
import java.io.File
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import kotlin.math.roundToInt

class RadarProductPane(var volume: RadarProductVolume, var tilt: Int) : JPanel() {
    var map: MapView = MapView()

    init {
        val countries = JSONObject(
            File("src/main/resources/geo/countries.geojson").readText(Charsets.UTF_8)
        )
        val counties = JSONObject(
            File("src/main/resources/geo/counties.json").readText(Charsets.UTF_8)
        )
        map.addLayer(RadarLayer(volume, tilt))
        map.addLayer(GeoJSONLayer(countries, 0.05f, Vector3f(0.8f)))
        map.addLayer(GeoJSONLayer(counties, 0.03f, Vector3f(0.8f)))

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
        productSelect.selectedItem = volume.product.displayName
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

        val tiltLbl = JLabel("Tilt: ${volume.scans[tilt].elevation.roundToInt()} deg")
        tiltLbl.alignmentX = JLabel.LEFT_ALIGNMENT

        val dateTime = ZonedDateTime.parse(volume.timeCoverageEnd)
        val localTime = dateTime.toLocalDateTime()

        val timeLbl = JLabel(formatDateTime(localTime))
        timeLbl.alignmentX = JLabel.RIGHT_ALIGNMENT

        row.add(tiltLbl)
        row.add(Box.createHorizontalGlue())
        row.add(timeLbl)

        header.add(Box.createVerticalStrut(4))
        header.add(row)
        val cmapBar = ColormapBar(volume.product.colormap, 250)
        cmapBar.maximumSize = Dimension(Int.MAX_VALUE, 25)
        add(cmapBar)
        add(header)
        add(map)
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d hh:mm:ss a")
        return fmt.format(dateTime)
    }

    fun render() {
        map.render()
    }
}