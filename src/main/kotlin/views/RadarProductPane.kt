package views

import RadarVolume
import map.MapView
import map.layers.RadarLayer
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import kotlin.math.roundToInt

class RadarProductPane(var volume: RadarVolume, var tilt: Int) : JPanel() {
    var map: MapView = MapView()

    init {
        map.addLayer(RadarLayer(volume, tilt))
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createLineBorder(Color.GREEN, 1)

        val header = JPanel()
        header.background = Color(50, 50, 50)
        header.layout = BoxLayout(header, BoxLayout.Y_AXIS)
        header.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val topRow = JPanel()
        topRow.isOpaque = false
        topRow.layout = BoxLayout(topRow, BoxLayout.X_AXIS)
        val productLbl = JLabel(volume.product.displayName)
        productLbl.alignmentX = JLabel.LEFT_ALIGNMENT
//        productLbl.putClientProperty("FlatLaf.styleClass", "h3")
        productLbl.putClientProperty("FlatLaf.style", "font: bold \$h3.regular.font");

        topRow.add(productLbl)
        topRow.add(Box.createHorizontalGlue())
        header.add(topRow)

        val row = JPanel()
        row.isOpaque = false
        row.layout = BoxLayout(row, BoxLayout.X_AXIS)

        val tiltLbl = JLabel("Tilt: ${volume.scans[tilt].elevation.roundToInt()} deg")
        tiltLbl.alignmentX = JLabel.LEFT_ALIGNMENT
        val timeLbl = JLabel("Updated ${volume.timeCoverageEnd}")
        timeLbl.alignmentX = JLabel.RIGHT_ALIGNMENT

        row.add(tiltLbl)
        row.add(Box.createHorizontalGlue())
        row.add(timeLbl)

        header.add(row)
        val cmapBar = ColormapBar(volume.product.colormap, 250)
        cmapBar.maximumSize = Dimension(Int.MAX_VALUE, 25)
        add(cmapBar)
        add(header)
        add(map)
    }

    fun render() {
        map.render()
    }
}