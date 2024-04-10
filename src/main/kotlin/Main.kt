import com.formdev.flatlaf.FlatDarkLaf
import org.intellij.lang.annotations.JdkConstants.BoxLayoutAxis
import org.intellij.lang.annotations.JdkConstants.FontStyle
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.border.Border
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin


fun main() {
    System.setProperty( "apple.laf.useScreenMenuBar", "true" )
    System.setProperty( "apple.awt.application.appearance", "system" )
    FlatDarkLaf.setup()
    //val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
    //println(file.variables)
    val window = JFrame("Azimuth")
    val panel = JPanel()
    val data = GLData()
    val canvas = object : AWTGLCanvas(data) {
        override fun initGL() {
            println("GL Version: ${effective.majorVersion}.${effective.minorVersion}")
            GL.createCapabilities()
            glClearColor(0f, 1.0f, 0.0f, 1.0f)
        }

        override fun paintGL() {
            val aspect: Float = width.toFloat() / height
            val now = System.currentTimeMillis() * 0.001
            val qwidth = abs(sin(now * 0.3)).toFloat()
            glClear(GL_COLOR_BUFFER_BIT)
            glViewport(0, 0, width, height)
            glBegin(GL_QUADS)
            glColor3f(0.4f, 0.6f, 0.8f)
            glVertex2f(-0.75f * (qwidth / aspect.toFloat()), 0.0f)
            glVertex2f(0f, -0.75f)
            glVertex2f(0.75f * (qwidth / aspect.toFloat()), 0f)
            glVertex2f(0f, +0.75f)
            glEnd()
            swapBuffers()
        }
    }


    panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
    panel.preferredSize = Dimension(300, 300)
    panel.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    val title = JLabel("SIDEBAR")
    title.font = Font("Arial", Font.BOLD, 20)
    panel.add(title)

    window.minimumSize = Dimension(1300, 700)
    window.layout = BorderLayout()
    window.add(canvas, BorderLayout.CENTER)
    window.add(panel, BorderLayout.WEST)
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    window.isVisible = true

    window.pack()
    SwingUtilities.invokeLater(object : Runnable {
        override fun run() {
            if (!canvas.isValid){
                GL.setCapabilities(null);
                return
            }
            canvas.render()
            SwingUtilities.invokeLater(this)
        }
    })
}