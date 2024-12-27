package views

import data.state.AppState
import meteo.radar.Product
import org.lwjgl.opengl.awt.GLData
import java.awt.GridLayout
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

class RadarMultiPane : JPanel() {
    private val productPanes: Array<RadarProductPane?> = arrayOfNulls(4)

    private val numPanes = 2
    init {
        layout = GridLayout(2,2)
        for(i in 0..<numPanes) {
            val glData = GLData()
//            glData.majorVersion = 4
//            glData.minorVersion = 6

            if(i > 0)
                glData.shareContext = productPanes[0]?.map

            productPanes[i] = RadarProductPane(AppState.activeVolume.value!!, Product.entries.getOrElse(i) {_ -> Product.REFLECTIVITY_HIRES}, 0, glData)
            add(productPanes[i])
        }
    }

    fun startRendering() {
        SwingUtilities.invokeLater {
            Timer(1000/60) {
                for(pane in productPanes) {
                    pane?.render()
                }
            }.start()
        }
    }
}