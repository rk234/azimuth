import com.formdev.flatlaf.FlatDarkLaf
import map.MapView
import meteo.radar.Colormap
import meteo.radar.Product
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.awt.GLData
import views.ColormapBar
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
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


    val window = JFrame()
    val panel = JPanel()
    val data = GLData()
    val canvas = MapView()

    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.preferredSize = Dimension(400, 300)
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    val title = JLabel("Sidebar")
    title.putClientProperty("FlatLaf.styleClass", "h2")
    panel.add(title)

    window.minimumSize = Dimension(1000, 700)
    window.layout = BorderLayout()
    val cmap = Product.REFLECTIVITY_HIRES.colormap
    val bar = ColormapBar(cmap, 500)
    window.add(bar, BorderLayout.NORTH)
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.WEST)
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true

    window.pack()
    SwingUtilities.invokeLater(object : Runnable {
        var prev = 0L;
        override fun run() {
            if (!canvas.isValid) {
                GL.setCapabilities(null)
                return
            }
            canvas.render()
            SwingUtilities.invokeLater(this)
//            println("FPS: ${1 / ((System.currentTimeMillis() - prev) / 1000f)}")
            prev = System.currentTimeMillis()
        }
    })
}