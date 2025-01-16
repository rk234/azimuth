package views.sidebar

import data.radar.RadarDataRepository
import data.state.AppState
import meteo.radar.RadarVolume
import views.StationPicker
import java.awt.Color
import javax.swing.*

class StationPanel : JPanel() {
    private val stationCodeLabel: JLabel
    private val changeStationButton: JButton
    private val stationLocationLabel: JLabel
    private val vcpLabel: JLabel

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
        changeStationButton.addActionListener {
            val stationPicker = StationPicker(AppState.radarDataProvider)
            stationPicker.addStationSelectListener { station ->
                if(station != null) {
                    println("Station selected $station and data loaded!")
                    AppState.activeVolume.value = RadarDataRepository.lastFile()
                }
            }
            stationPicker.isVisible = true
        }
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