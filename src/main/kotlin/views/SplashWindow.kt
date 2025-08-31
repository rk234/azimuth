package views

import utils.ProgressListener
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Image
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import javax.swing.*


class SplashWindow : JFrame(), ProgressListener {
    private class SplashPanel(private val splashImage: Image) : JPanel(BorderLayout()) {
        override fun paintComponent(g: java.awt.Graphics) {
            super.paintComponent(g)
            g.drawImage(splashImage, 0, 0, width, height, this)
        }
    }

    val progressBar = JProgressBar(0, 100)
    val progressLabel = JLabel("Loading...")

    init {
        progressLabel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        val splashImage = ImageIO.read(File("src/main/resources/splash/lwx_splash.jpg"))
        val panel = SplashPanel(splashImage)

        panel.add(progressBar, BorderLayout.SOUTH)
        panel.add(progressLabel, BorderLayout.NORTH)
        contentPane = panel
        isUndecorated = true
        minimumSize = Dimension(640, 480)
        pack()
        setLocationRelativeTo(null)
    }

    override fun notifyProgress(progress: Double?, message: String) {
        SwingUtilities.invokeLater {
            progressLabel.text = message

            if (progress == null) {
                progressBar.isIndeterminate = true
            } else {
                progressBar.isIndeterminate = false
                progressBar.value = (progress * 100).toInt()
            }
        }
    }
}