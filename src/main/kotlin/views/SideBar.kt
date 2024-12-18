package views

import java.awt.Dimension
import javax.swing.*

class SideBar : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        preferredSize = Dimension(400, 300)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        val title = JLabel("Sidebar")
        title.alignmentX = JLabel.LEFT_ALIGNMENT
        title.putClientProperty("FlatLaf.styleClass", "h2")
        add(title)
        val tabs = JTabbedPane()

        val list = JList<String>(
            (0..<200).map { "Warning ${it.toString()}" }.toTypedArray()
        )

        tabs.addTab("Radar", JLabel("Radar tab"))
        tabs.addTab("Warnings", JScrollPane(list))
        tabs.addTab("Map", JLabel("Map tab"))
        tabs.alignmentX = JTabbedPane.LEFT_ALIGNMENT
        add(tabs)
    }
}