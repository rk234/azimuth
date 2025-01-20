package views

import kotlinx.coroutines.Job
import utils.ProgressListener
import javax.swing.*

class ProgressDialog(title: String) : JFrame(title), ProgressListener {
    private val panel = JPanel()
    private val messageLbl: JLabel = JLabel()
    private val progressBar: JProgressBar = JProgressBar()

    init {
        setSize(300, 100)
        setLocationRelativeTo(parent)
        isResizable = false
        isUndecorated = true

        contentPane = panel
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(8,8,8,8)

        messageLbl.alignmentX = JLabel.LEFT_ALIGNMENT
        progressBar.alignmentX = JProgressBar.LEFT_ALIGNMENT

        add(messageLbl)
        add(Box.createVerticalStrut(8))
        add(progressBar)
    }

    override fun notifyProgress(progress: Double?, message: String) {
        SwingUtilities.invokeLater {
            messageLbl.text = message
            progressBar.isIndeterminate = progress == null
            progressBar.value = (progress?.times(100)?.toInt() ?: 0)
        }
    }
}