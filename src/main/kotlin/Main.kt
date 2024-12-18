import com.formdev.flatlaf.FlatDarkLaf
import meteo.radar.Product
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.awt.GLData
import ucar.nc2.NetcdfFiles
import views.RadarProductPane
import views.SideBar
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionListener
import javax.swing.Action
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
    val vol = RadarVolume(file, Product.REFLECTIVITY_HIRES)

    val window = JFrame()
    val panel = JPanel()
    val productPane = RadarProductPane(vol, 0)


    window.minimumSize = Dimension(1000, 700)
    window.layout = BorderLayout()

    window.add(productPane, BorderLayout.CENTER)
    window.add(SideBar(), BorderLayout.WEST)
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true

    window.pack()
    SwingUtilities.invokeLater(object : Runnable {
        override fun run() {
            if (!productPane.isValid) {
                GL.setCapabilities(null)
                return
            }

            Timer(1000 / 60) {
                productPane.render()
            }.start()
        }
    })
}