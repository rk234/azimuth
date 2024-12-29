package views.sidebar

import com.formdev.flatlaf.extras.FlatSVGIcon
import views.LoopControlPanel
import views.PaneLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class RadarPanel : JPanel() {
    private val paneLayoutSelect: PaneLayoutSelect

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        alignmentX = LEFT_ALIGNMENT

        paneLayoutSelect = PaneLayoutSelect()
        paneLayoutSelect.alignmentX = LEFT_ALIGNMENT
        add(paneLayoutSelect)

        val loopControlPanel = LoopControlPanel()
        loopControlPanel.alignmentX = LEFT_ALIGNMENT
        add(loopControlPanel)
    }


    fun addPaneLayoutChangeListener(listener: (PaneLayout) -> Unit) {
        paneLayoutSelect.addListener(listener)
    }
}