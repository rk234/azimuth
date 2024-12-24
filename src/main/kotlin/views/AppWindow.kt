package views

import data.state.AppState
import data.state.UserPrefs
import meteo.radar.Product
import org.lwjgl.opengl.GL
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.Timer

class AppWindow : JFrame("Azimuth") {
    val radarAutoPollTimer = Timer(1000 * UserPrefs.radarAutoPollFrequencySec(), ::onRadarAutoPoll)

    init {
        val productPane = RadarProductPane(AppState.activeVolume.value!!, Product.REFLECTIVITY_HIRES, 0)

        minimumSize = Dimension(1000, 700)
        layout = BorderLayout()

        add(productPane, BorderLayout.CENTER)
        add(SideBar(), BorderLayout.WEST)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        pack()
        SwingUtilities.invokeLater(object : Runnable {
            override fun run() {
                if (!productPane.isValid) {
                    GL.setCapabilities(null)
                    return
                }

                Timer(1000/60) {
                    productPane.render()
                }.start()

                radarAutoPollTimer.start()
            }
        })
    }

    fun onRadarAutoPoll(actionEvent: ActionEvent) {
        print("Autopolling!")
    }
}