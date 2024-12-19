import com.formdev.flatlaf.FlatDarkLaf
import data.ShaderManager
import meteo.radar.Product
import meteo.radar.RadarProductVolume
import org.lwjgl.opengl.GL
import ucar.nc2.NetcdfFiles
import views.RadarProductPane
import views.SideBar
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
    val vol = RadarProductVolume(file, Product.REFLECTIVITY_HIRES)

    val window = JFrame()
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

            ShaderManager.init()

            Timer(1000 / 60) {
                productPane.render()
            }.start()
        }
    })
}