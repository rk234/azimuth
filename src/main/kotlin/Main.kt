import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.util.SystemInfo
import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.resources.ColormapManager
import data.resources.ColormapTextureManager
import data.resources.GeoJSONManager
import data.state.AppState
import data.state.UserPrefs
import map.layers.GeoJSONLayer
import meteo.radar.RadarVolume
import meteo.radar.VolumeFileHandle
import okhttp3.Dispatcher
import org.lwjgl.opengl.GL
import ucar.nc2.NetcdfFiles
import views.AppWindow
import views.SplashWindow
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")

    // This resets user prefs to defaults, remove when not in dev
    UserPrefs.reset()

    FlatDarkLaf.setup()

    if(SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    val splash = SplashWindow()
    splash.isVisible = true

    splash.onProgress(null, "Loading Map Data...")
    GeoJSONManager.init()
    splash.onProgress(1.0, "Done Loading Map Data!")
    splash.onProgress(null, "Loading Colormap Data...")
    ColormapManager.init()
    splash.onProgress(1.0, "Done Loading Colormap Data!")

    AppState.radarDataService.addProgressListener(splash)
    AppState.radarDataService.init()


    AppState.init()
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true
}