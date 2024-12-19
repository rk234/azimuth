package views

import java.awt.Color
import javax.swing.*

class StationPanel : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val stationHeader = JPanel()
        stationHeader.layout = BoxLayout(stationHeader, BoxLayout.X_AXIS)

        val title = JLabel("KLWX")
        title.alignmentX = JLabel.LEFT_ALIGNMENT
        title.putClientProperty("FlatLaf.styleClass", "h1")
        title.foreground = Color.CYAN
        stationHeader.add(title)
        stationHeader.add(Box.createHorizontalGlue())
        stationHeader.add(JButton("Change Station"))
        stationHeader.alignmentX = JPanel.LEFT_ALIGNMENT

        add(stationHeader)
        val stationLocation = JLabel("Baltimore/Washington")
        stationLocation.alignmentX = JLabel.LEFT_ALIGNMENT
        val mode = JLabel("Precip Mode (VCP 212)")
        mode.alignmentX = JLabel.LEFT_ALIGNMENT
        add(stationLocation)
        add(mode)
        val slider = JSlider()
        slider.alignmentX = JSlider.LEFT_ALIGNMENT
        add(slider)
    }
}