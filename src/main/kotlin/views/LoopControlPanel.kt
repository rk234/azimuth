package views

import data.state.AppState
import java.awt.Color
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.*

class LoopControlPanel : JPanel() {
    private val frameSlider: JSlider = JSlider()
    private val loopFrameSelect: JComboBox<Int> = JComboBox()
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        val loopControlLabel = JLabel("Loop Controls")
        loopControlLabel.alignmentX = LEFT_ALIGNMENT
        loopControlLabel.putClientProperty("FlatLaf.style", "font: bold")
        add(loopControlLabel)
        add(Box.createVerticalStrut(8))

        loopFrameSelect.addItem(5)
        loopFrameSelect.addItem(10)
        loopFrameSelect.addItem(15)
        loopFrameSelect.addItem(20)
        loopFrameSelect.alignmentX = JComboBox.LEFT_ALIGNMENT
        add(object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                alignmentX = LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, 30)
                add(JLabel("Frames:"))
                add(Box.createHorizontalStrut(8))
                add(loopFrameSelect)
            }
        })

        add(Box.createVerticalStrut(8))

        frameSlider.alignmentX = JSlider.LEFT_ALIGNMENT
        add(frameSlider)

        add(Box.createVerticalStrut(8))
        val btnGroup = ButtonGroup()
        btnGroup.add(JButton("<"))
        btnGroup.add(JButton("Play"))
        btnGroup.add(JButton("Pause"))
        btnGroup.add(JButton(">"))

        val btnPanel = JPanel()
        btnPanel.maximumSize = Dimension(Int.MAX_VALUE, 50)
        btnPanel.alignmentX = LEFT_ALIGNMENT
        btnPanel.layout = GridLayout(1, 0, 4, 0)

        btnGroup.elements.toList().forEach { it.alignmentX = JButton.CENTER_ALIGNMENT }
        btnGroup.elements.toList().forEach { btnPanel.add(it) }
        add(btnPanel)
    }
}