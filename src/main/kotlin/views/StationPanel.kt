package views

import data.state.AppState
import java.awt.Color
import javax.swing.*

class StationPanel : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val stationHeader = JPanel()
        stationHeader.layout = BoxLayout(stationHeader, BoxLayout.X_AXIS)

        val title = JLabel(AppState.activeVolume.value!!.station.code)
        title.alignmentX = JLabel.LEFT_ALIGNMENT
        title.putClientProperty("FlatLaf.styleClass", "h1")
        title.foreground = Color.CYAN
        stationHeader.add(title)
        stationHeader.add(Box.createHorizontalGlue())
        stationHeader.add(JButton("Change Station"))
        stationHeader.alignmentX = JPanel.LEFT_ALIGNMENT

        add(stationHeader)
        val stationLocation = JLabel(AppState.activeVolume.value!!.station.name.replace(",", ", "))
        stationLocation.alignmentX = JLabel.LEFT_ALIGNMENT
        val mode = JLabel("VCP: ${AppState.activeVolume.value!!.vcp} (${AppState.activeVolume.value!!.vcpName})")
        mode.alignmentX = JLabel.LEFT_ALIGNMENT
        add(stationLocation)
        add(mode)
    }
}