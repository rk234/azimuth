package views.sidebar

import views.PaneLayout
import java.awt.Dimension
import javax.swing.*

class SideBar : JPanel() {
    val radarPanel: RadarPanel


    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        preferredSize = Dimension(400, 300)

        val stationPanel = StationPanel()
        stationPanel.alignmentX = JPanel.LEFT_ALIGNMENT
        add(stationPanel)

        val tabs = JTabbedPane()


        radarPanel = RadarPanel()

        tabs.addTab("Radar", radarPanel)
                val mockWarnings = listOf(
            data.warnings.Warning(
                type = "Tornado Warning",
                message = "A tornado is imminent. Take shelter now!",
                areaDesc = "Central County",
                sent = java.time.ZonedDateTime.now(),
                effective = java.time.ZonedDateTime.now(),
                onset = java.time.ZonedDateTime.now(),
                expires = java.time.ZonedDateTime.now().plusHours(1)
            ),
            data.warnings.Warning(
                type = "Severe Thunderstorm Warning",
                message = "Severe thunderstorm with large hail and damaging winds.",
                areaDesc = "East County",
                sent = java.time.ZonedDateTime.now(),
                effective = java.time.ZonedDateTime.now(),
                onset = java.time.ZonedDateTime.now(),
                expires = java.time.ZonedDateTime.now().plusHours(2)
            ),
            data.warnings.Warning(
                type = "Flash Flood Warning",
                message = "Flash flooding is already occurring or imminent.",
                areaDesc = "West County",
                sent = java.time.ZonedDateTime.now(),
                effective = java.time.ZonedDateTime.now(),
                onset = java.time.ZonedDateTime.now(),
                expires = java.time.ZonedDateTime.now().plusHours(3)
            )
        ).flatMap{ warning -> listOf(
            warning.copy(type = "Tornado Warning", message = "Tornado warning for ${warning.areaDesc}"),
            warning.copy(type = "Severe Thunderstorm Warning", message = "Severe thunderstorm warning for ${warning.areaDesc}"),
            warning.copy(type = "Flash Flood Warning", message = "Flash flood warning for ${warning.areaDesc}")
        )}
        tabs.addTab("Warnings", WarningPanel(mockWarnings))
        tabs.addTab("Map", JLabel("Map tab"))

        tabs.alignmentX = JTabbedPane.LEFT_ALIGNMENT
        add(tabs)
    }

    fun onPaneLayoutChange(listener: (PaneLayout) -> Unit) {
        radarPanel.addPaneLayoutChangeListener(listener)
    }
}