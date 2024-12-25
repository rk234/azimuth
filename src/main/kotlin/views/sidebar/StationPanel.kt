package views.sidebar

import data.state.AppState
import meteo.radar.RadarVolume
import java.awt.Color
import javax.swing.*

class StationPanel : JPanel() {
    val stationCodeLabel: JLabel
    val changeStationButton: JButton
    val stationLocationLabel: JLabel
    val vcpLabel: JLabel

    init {
        AppState.activeVolume.onChange(::handleVolumeChange)

        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val stationHeader = JPanel()
        stationHeader.layout = BoxLayout(stationHeader, BoxLayout.X_AXIS)

        stationCodeLabel = JLabel(AppState.activeVolume.value!!.station.code)
        stationCodeLabel.alignmentX = JLabel.LEFT_ALIGNMENT
        stationCodeLabel.putClientProperty("FlatLaf.styleClass", "h1")
        stationCodeLabel.foreground = Color.CYAN
        stationHeader.add(stationCodeLabel)
        stationHeader.add(Box.createHorizontalGlue())

        changeStationButton = JButton("Change Station")
        stationHeader.add(changeStationButton)
        stationHeader.alignmentX = JPanel.LEFT_ALIGNMENT

        add(stationHeader)
        stationLocationLabel = JLabel(AppState.activeVolume.value!!.station.name.replace(",", ", "))
        stationLocationLabel.alignmentX = JLabel.LEFT_ALIGNMENT
        vcpLabel = JLabel("VCP: ${AppState.activeVolume.value!!.vcp} (${AppState.activeVolume.value!!.vcpName})")
        vcpLabel.alignmentX = JLabel.LEFT_ALIGNMENT
        add(stationLocationLabel)
        add(vcpLabel)
    }

    fun handleVolumeChange(newVolume: RadarVolume?) {
        val station = newVolume?.station
        stationCodeLabel.text = station?.code ?: "UNKN"
        stationLocationLabel.text = station?.name?.replace(",", ", ") ?: "Unknown station"
        vcpLabel.text = "VCP: ${AppState.activeVolume.value!!.vcp} (${AppState.activeVolume.value!!.vcpName})" ?: "Unknown VCP"
    }
}