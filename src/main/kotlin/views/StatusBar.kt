package views

import utils.ProgressListener
import java.awt.Color
import java.awt.Dimension
import javax.swing.*
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class StatusBar(val multiPane: RadarMultiPane) : JPanel(), ProgressListener {
    private val fpsLabel: JLabel = JLabel("FPS: ??")
    private val progressBar: JProgressBar = JProgressBar()
    private val statusLabel: JLabel = JLabel("READY")
    private val statusPanel = JPanel()

    private val progressIndicatorPanel = JPanel()
    private val updateInLabel = JLabel("Update in ?? seconds")

    private val readyColor = Color(0x43FF46)
    private val pendingColor = Color(0xFFBF50)

    var nextUpdateTime = TimeSource.Monotonic.markNow() + 10.seconds

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color(0x2f2f2f))
        alignmentX = LEFT_ALIGNMENT
        minimumSize = Dimension(Int.MAX_VALUE,100)

        add(object : JPanel() {
            init {
                border = BorderFactory.createEmptyBorder(4,4,4,4)
                layout = BoxLayout(this, BoxLayout.X_AXIS)
                fpsLabel.alignmentX = JLabel.LEFT_ALIGNMENT
                fpsLabel.minimumSize = Dimension(300, 100)
                updateInLabel.alignmentX = JLabel.LEFT_ALIGNMENT
                add(fpsLabel)
                add(Box.createHorizontalStrut(4))
//                add(JSeparator(SwingConstants.VERTICAL))
                add(Box.createHorizontalStrut(4))
                add(updateInLabel)
            }
        })
        add(Box.createHorizontalGlue())

        progressBar.maximumSize = Dimension(400,50)
        progressIndicatorPanel.layout = BoxLayout(progressIndicatorPanel, BoxLayout.X_AXIS)
        progressIndicatorPanel.add(progressBar)

        add(Box.createRigidArea(Dimension(4, 0)))

        statusPanel.alignmentX = RIGHT_ALIGNMENT
        statusPanel.minimumSize = Dimension(300,100)
        statusPanel.maximumSize = Dimension(400,100)
        statusPanel.background = readyColor
        statusLabel.foreground = Color.DARK_GRAY
        statusLabel.putClientProperty("FlatLaf.style", "font: bold")
        statusPanel.add(statusLabel)

        progressIndicatorPanel.add(statusPanel)

        add(progressIndicatorPanel)

        startFPSUpdateTimer()
        startNextUpdateTimer()
    }

    private fun startFPSUpdateTimer(): Timer {
        val timer = Timer(500) {
            fpsLabel.text = "FPS: %02d".format(multiPane.fps)
        }

        timer.start()
        return timer
    }

    private fun startNextUpdateTimer(): Timer {
        val timer = Timer(1000)  {
            val currentTime = TimeSource.Monotonic.markNow()
            val toUpdate = nextUpdateTime - currentTime
            if(toUpdate >= 0.seconds) {
                updateInLabel.text = "Update in ${toUpdate.inWholeSeconds} sec"
            } else {
                updateInLabel.text = "Checking for new data..."
            }
        }

        timer.start()
        return timer
    }

    override fun notifyProgress(progress: Double?, message: String) {
        if(progress != null && progress >= 1.0) {
//            statusLabel.text = "READY"
            statusPanel.background = readyColor
            progressBar.isVisible = false
            return
        } else if(progress != null) {
            progressBar.isVisible = true
            progressBar.isIndeterminate = false
            progressBar.value = Math.round(progress.toFloat() * 100)
        } else {
            progressBar.isVisible = true
            progressBar.isIndeterminate = true
        }
        statusPanel.background = pendingColor
        statusLabel.text = message
    }
}