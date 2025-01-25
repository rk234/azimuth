package views.sidebar

import javax.swing.BoxLayout
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

class WarningPanel : JPanel() {
//    private val warningList = JList<Warning>()
    private val listScrollPane = JScrollPane()

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
    }
}