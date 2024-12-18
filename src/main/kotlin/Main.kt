import com.formdev.flatlaf.FlatDarkLaf
import meteo.radar.Product
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.awt.GLData
import ucar.nc2.NetcdfFiles
import views.RadarProductPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
    val vol = RadarVolume(file, Product.REFLECTIVITY_HIRES)

    val window = JFrame()
    val panel = JPanel()
    val productPane = RadarProductPane(vol, 0)

    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.preferredSize = Dimension(400, 300)
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    val title = JLabel("Sidebar")
    title.putClientProperty("FlatLaf.styleClass", "h2")
    panel.add(title)

    window.minimumSize = Dimension(1000, 700)
    window.layout = BorderLayout()

    window.add(productPane, BorderLayout.CENTER)
    window.add(panel, BorderLayout.WEST)
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true

    window.pack()
    SwingUtilities.invokeLater(object : Runnable {
        var prev = 0L;
        override fun run() {
            if (!productPane.isValid) {
                GL.setCapabilities(null)
                return
            }
            productPane.render()
            SwingUtilities.invokeLater(this)
            prev = System.currentTimeMillis()
        }
    })
}