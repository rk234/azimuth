package views

//import com.sun.java.swing.ui.StatusBar
import data.state.AppState
import data.state.UserPrefs
import meteo.radar.Product
import views.sidebar.SideBar
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JFrame
import javax.swing.Timer

class AppWindow : JFrame("Azimuth") {

    init {
        val multiPane = RadarMultiPane(PaneLayout.SINGLE)
        val sideBar = SideBar()

        sideBar.onPaneLayoutChange { layout ->
            multiPane.setPaneLayout(layout)
        }

        minimumSize = Dimension(1000, 700)
        layout = BorderLayout()

        add(multiPane, BorderLayout.CENTER)
        add(sideBar, BorderLayout.WEST)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        pack()
        isVisible = true

        multiPane.startRendering()
    }

    fun onRadarAutoPoll(actionEvent: ActionEvent) {
        AppState.pollRadarData()
    }
}