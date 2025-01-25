package views

//import com.sun.java.swing.ui.StatusBar
import data.radar.RadarCache
import data.radar.RadarDataRepository
import data.state.AppState
import data.state.AppState.activeVolume
import data.state.AppState.radarDataProvider
import data.state.AppState.radarDataService
import data.state.UserPrefs
import kotlinx.coroutines.*
import meteo.radar.Product
import meteo.radar.RadarVolume
import utils.ProgressListener
import views.sidebar.SideBar
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Menu
import java.awt.MenuBar
import java.awt.event.ActionEvent
import javax.swing.Action
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JMenuBar
import javax.swing.Timer
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class AppWindow : JFrame("Azimuth") {

    private val radarAutoPollTimer = Timer(UserPrefs.radarAutoPollFrequencySec() * 1000, ::onRadarAutoPoll)
    private val cleanCacheTimer = Timer(1000 * UserPrefs.cacheCleanupFrequencySec(), ::cleanCache)

    private val multiPane: RadarMultiPane
    private val sideBar: SideBar
    private val statusBar: StatusBar

    private val scope = MainScope()

    init {
        AppState.window = this
        multiPane = RadarMultiPane(PaneLayout.SINGLE)
        sideBar = SideBar()
        statusBar = StatusBar(multiPane)

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
        cleanCacheTimer.start()
    }

    fun pauseAutoPoll() {
        radarAutoPollTimer.stop()
    }

    fun resumeAutoPoll() {
        radarAutoPollTimer.start()
    }

    fun cleanCache(actionEvent: ActionEvent? = null) {
        println("Cleaning Cache...")
        val sb = StringBuilder()
        RadarCache.clearCache(UserPrefs.cacheSizeBytes(), sb)
        println(sb)
    }

    fun onRadarAutoPoll(actionEvent: ActionEvent) {
        scope.launch(Dispatchers.IO) {
            statusBar.nextUpdateTime = TimeSource.Monotonic.markNow()
            val data = pollRadarData()
            if(data != null) {
                if(RadarDataRepository.getHandle(data.handle) == null) {
                    RadarDataRepository.addDataFile(data)
                    activeVolume.value = RadarDataRepository.lastFile()
                }
            } else {
                println("No new data..")
            }
            statusBar.notifyProgress(1.0, "READY")
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