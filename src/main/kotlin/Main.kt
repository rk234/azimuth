import com.formdev.flatlaf.FlatDarkLaf
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel

fun main() {
    System.setProperty( "apple.laf.useScreenMenuBar", "true" )
    System.setProperty( "apple.awt.application.appearance", "system" )
    FlatDarkLaf.setup()
    //val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
    //println(file.variables)
    val window = JFrame("Azimuth")
    val panel = JPanel()

    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

    panel.add(JButton("Click Me!"))

    window.setSize(500, 400)
    window.contentPane = panel
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true
}