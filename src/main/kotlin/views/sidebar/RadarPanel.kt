package views.sidebar

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGUtils
import views.LoopControlPanel
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class RadarPanel : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        alignmentX = LEFT_ALIGNMENT

        add(object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

                val paneControlLabel = JLabel("Radar Panes")
                paneControlLabel.alignmentX = LEFT_ALIGNMENT
                paneControlLabel.putClientProperty("FlatLaf.style", "font: bold")
                add(paneControlLabel)
                add(Box.createVerticalStrut(8))

                add(object : JPanel() {
                    init {
                        alignmentX = LEFT_ALIGNMENT
                        maximumSize = Dimension(Int.MAX_VALUE,40)
                        layout = GridLayout(1, 0, 4, 0)
                        val btnGroup = ButtonGroup()
                        val singlePaneBtn = JToggleButton(loadIcon("single_pane.svg", 20, 20))
                        singlePaneBtn.maximumSize = Dimension(Int.MAX_VALUE, 40)
                        singlePaneBtn.toolTipText = "Single Pane View"
                        add(singlePaneBtn)
                        btnGroup.add(singlePaneBtn)

                        val dualPaneBtn = JToggleButton(loadIcon("dual_pane.svg", 20, 20))
                        dualPaneBtn.maximumSize = Dimension(Int.MAX_VALUE, 40)
                        dualPaneBtn.toolTipText = "Dual Pane View"
                        add(dualPaneBtn)
                        btnGroup.add(dualPaneBtn)

                        val quadPaneBtn = JToggleButton(loadIcon("quad_pane.svg", 20, 20))
                        quadPaneBtn.maximumSize = Dimension(Int.MAX_VALUE, 40)
                        quadPaneBtn.toolTipText = "Quad Pane View"
                        add(quadPaneBtn)
                        btnGroup.add(quadPaneBtn)
                    }
                })
            }
        })

        val loopControlPanel = LoopControlPanel()
        loopControlPanel.alignmentX = LEFT_ALIGNMENT
        add(loopControlPanel)
    }

    fun loadIcon(name: String, width: Int, height: Int): FlatSVGIcon {
        val icon = FlatSVGIcon("icons/$name", width, height)
        icon.colorFilter = FlatSVGIcon.ColorFilter()
        icon.colorFilter.add(Color.BLACK, Color.BLACK, Color.LIGHT_GRAY)
        return icon
    }
}