package views

import java.awt.Dimension
import javax.swing.*

class LoopControlPanel : JPanel() {
    private val frameSlider: JSlider = JSlider()
    private val loopFrameSelect: JComboBox<Int> = JComboBox()
    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = BorderFactory.createEmptyBorder(8, 8, 8, 8)

        loopFrameSelect.addItem(5)
        loopFrameSelect.addItem(10)
        loopFrameSelect.addItem(15)
        loopFrameSelect.addItem(20)
        loopFrameSelect.alignmentX = JComboBox.LEFT_ALIGNMENT
        add(object : JPanel() {
            init {
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                alignmentX = LEFT_ALIGNMENT
                maximumSize = Dimension(Int.MAX_VALUE, 50)
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
        btnPanel.alignmentX = LEFT_ALIGNMENT
        btnPanel.layout = BoxLayout(btnPanel, BoxLayout.X_AXIS)

        btnGroup.elements.toList().forEach { it.alignmentX = JButton.CENTER_ALIGNMENT }
        btnGroup.elements.toList().forEach { btnPanel.add(it) }
        add(btnPanel)
    }
}