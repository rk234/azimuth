package views

//import com.sun.java.swing.ui.StatusBar
import data.radar.RadarDataRepository
import data.state.AppState
import data.state.AppState.activeVolume
import data.state.AppState.radarDataProvider
import data.state.AppState.radarDataService
import data.state.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import meteo.radar.Product
import meteo.radar.RadarVolume
import utils.ProgressListener
import views.sidebar.SideBar
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Menu
import java.awt.MenuBar
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.Timer
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class AppWindow : JFrame("Azimuth") {

    val radarAutoPollTimer = Timer(UserPrefs.radarAutoPollFrequencySec() * 1000, ::onRadarAutoPoll)
    val multiPane: RadarMultiPane
    val sideBar: SideBar
    val statusBar: StatusBar

    init {
        multiPane = RadarMultiPane(PaneLayout.SINGLE)
        sideBar = SideBar()
        statusBar = StatusBar(multiPane)

        jMenuBar = JMenuBar()
        jMenuBar.add(JButton("Toggle Sidebar"))

        sideBar.onPaneLayoutChange { layout ->
            multiPane.setPaneLayout(layout)
        }

        minimumSize = Dimension(1000, 700)
        layout = BorderLayout()

        add(multiPane, BorderLayout.CENTER)
        add(sideBar, BorderLayout.WEST)
        add(statusBar, BorderLayout.SOUTH)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        pack()
        isVisible = true

        radarDataProvider.addProgressListener(statusBar)
        multiPane.startRendering()
        radarAutoPollTimer.start()
    }

    fun onRadarAutoPoll(actionEvent: ActionEvent) {
        GlobalScope.launch(Dispatchers.IO) {
            statusBar.nextUpdateTime = TimeSource.Monotonic.markNow()
            val data = pollRadarData()
            if(data != null) {
                RadarDataRepository.addDataFile(data)
                activeVolume.value = RadarDataRepository.lastFile()
            } else {
                println("No new data..")
            }
            statusBar.nextUpdateTime = TimeSource.Monotonic.markNow() + UserPrefs.radarAutoPollFrequencySec().seconds
        }
    }

    private suspend fun pollRadarData(progressListener: ProgressListener? = null): RadarVolume? = coroutineScope {
        val handle = radarDataService.poll() ?: return@coroutineScope null

        val file = radarDataProvider.getDataFile(handle)
        if(file != null) {
            println("downloaded new radar data!")
            return@coroutineScope RadarVolume(file, handle)
//            RadarDataRepository.addDataFile(RadarVolume(file, handle))
//            activeVolume.value = RadarDataRepository.lastFile()
        } else {
            println("could not find new radar data!")
        }
        return@coroutineScope null
    }
}