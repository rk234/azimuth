import com.formdev.flatlaf.FlatDarkLaf
import java.awt.Color
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.GroupLayout.Alignment
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

    val btn = JButton("Click me!")
    btn.alignmentX = JButton.LEFT_ALIGNMENT
    val box = JPanel()
    box.background = Color.RED
    box.alignmentX = JPanel.LEFT_ALIGNMENT

    btn.addActionListener(ActionListener { 
        e: ActionEvent? -> println("Clicked!")
    })
    panel.add(btn)
    panel.add(box)

    window.setSize(500, 400)
    window.contentPane = panel
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true
}