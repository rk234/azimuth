package views

import RadarVolume
import map.MapView
import javax.swing.*
import kotlin.math.roundToInt

class RadarProductPane(var volume: RadarVolume, var tilt: Int) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val header = JPanel()
        header.layout = BoxLayout(header, BoxLayout.Y_AXIS)

        val productLbl = JLabel(volume.product.displayName)
        header.add(productLbl)

        val row = JPanel()
        row.layout = BoxLayout(row, BoxLayout.X_AXIS)

        val tiltLbl = JLabel("Tilt: ${volume.scans[tilt].elevation.roundToInt()} deg")
        tiltLbl.alignmentX = JLabel.LEFT_ALIGNMENT
        val timeLbl = JLabel("Updated ${volume.timeCoverageEnd}")
        timeLbl.alignmentX = JLabel.RIGHT_ALIGNMENT

        row.add(tiltLbl)
        row.add(Box.createHorizontalGlue())
        row.add(timeLbl)

        header.add(row)
        header.add(ColormapBar(volume.product.colormap, 250))
        add(header)
        add(MapView())
    }
}