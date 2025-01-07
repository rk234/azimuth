package views

import data.radar.RadarDataRepository
import data.state.AppState
import utils.loadIcon
import java.awt.Dimension
import java.awt.GridLayout
import java.awt.event.ActionEvent
import javax.swing.*

class LoopControlPanel : JPanel() {
    private val frameSlider: JSlider = JSlider()
    private val loopFrameSelect: JComboBox<Int> = JComboBox()

    private val togglePlayBtn: JButton = JButton(loadIcon("loop/play.svg", 20, 20))

    private val loopTimer: Timer = Timer(500, ::updateLoop)
    private var loopFrame = 0

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
        loopFrameSelect.alignmentX = LEFT_ALIGNMENT
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

        frameSlider.alignmentX = LEFT_ALIGNMENT
        frameSlider.minimum = 0
        frameSlider.maximum = AppState.numLoopFrames.value-1
        frameSlider.addChangeListener {
            if(frameSlider.value != loopFrame) {
                if(loopTimer.isRunning) {
                    togglePlayBtn.icon = loadIcon("loop/play.svg", 20, 20)
                    loopTimer.stop()
                }
                loopFrame = frameSlider.value
                setVolumeFrame(loopFrame)
            }
        }

        add(frameSlider)

        add(Box.createVerticalStrut(8))
        val btnGroup = ButtonGroup()
        val prevFrameBtn = JButton(loadIcon("loop/prev.svg", 20, 20))
        btnGroup.add(prevFrameBtn)

        togglePlayBtn.addActionListener {
            if(loopTimer.isRunning) {
                loopTimer.stop()
                togglePlayBtn.icon = loadIcon("loop/play.svg", 20, 20)
            } else {
                loopTimer.start()
                togglePlayBtn.icon = loadIcon("loop/pause.svg", 20, 20)
            }
        }

        btnGroup.add(togglePlayBtn)

        val nextFrameBtn = JButton(loadIcon("loop/next.svg", 20, 20))
        btnGroup.add(nextFrameBtn)

        val btnPanel = JPanel()
        btnPanel.maximumSize = Dimension(Int.MAX_VALUE, 50)
        btnPanel.alignmentX = LEFT_ALIGNMENT
        btnPanel.layout = GridLayout(1, 0, 4, 0)

        btnGroup.elements.toList().forEach { it.alignmentX = CENTER_ALIGNMENT }
        btnGroup.elements.toList().forEach { btnPanel.add(it) }
        add(btnPanel)
    }

    private fun updateLoop(e: ActionEvent) {
//        println("Looping frame: ${loopFrame}")
        if(loopFrame < AppState.numLoopFrames.value-1) {
            loopFrame++
        } else {
            loopFrame = 0
        }

        frameSlider.value = loopFrame

        setVolumeFrame(loopFrame)
    }

    private fun setVolumeFrame(frameIndex: Int) {
        val volume = RadarDataRepository.get(frameIndex)

        if(volume != null) {
            AppState.activeVolume.value = volume
        }
    }
}