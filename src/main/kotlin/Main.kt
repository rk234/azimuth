import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.util.SystemInfo
import data.resources.ColormapManager
import data.resources.GeoJSONManager
import data.state.AppState
import data.state.UserPrefs
import kotlinx.coroutines.runBlocking
import views.AppWindow
import views.SplashWindow
import javax.swing.JDialog
import javax.swing.JFrame

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")

    // This resets user prefs to defaults, remove when not in dev
    UserPrefs.reset()

    FlatDarkLaf.setup()

    val splash = SplashWindow()
    splash.isVisible = true

    splash.notifyProgress(null, "Loading Map Data...")
    GeoJSONManager.init()
    splash.notifyProgress(1.0, "Done Loading Map Data!")
    splash.notifyProgress(null, "Loading Colormap Data...")
    ColormapManager.init()
    splash.notifyProgress(1.0, "Done Loading Colormap Data!")

    AppState.radarDataService.addProgressListener(splash)
    AppState.radarDataService.init()

    if(SystemInfo.isLinux) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
    }

    runBlocking {
        AppState.init(splash)
    }
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true
}