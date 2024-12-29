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

        val list = JList<String>(
            (0..<200).map { "Warning ${it.toString()}" }.toTypedArray()
        )

        radarPanel = RadarPanel()

        tabs.addTab("Radar", radarPanel)
        tabs.addTab("Warnings", JScrollPane(list))
        tabs.addTab("Map", JLabel("Map tab"))

        tabs.alignmentX = JTabbedPane.LEFT_ALIGNMENT
        add(tabs)
    }

    fun onPaneLayoutChange(listener: (PaneLayout) -> Unit) {
        radarPanel.addPaneLayoutChangeListener(listener)
    }
}