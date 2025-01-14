import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.util.SystemInfo
import data.resources.ColormapManager
import data.resources.GeoJSONManager
import data.state.AppState
import data.state.UserPrefs
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
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

    val scope = MainScope()

    scope.launch(Dispatchers.IO) {
        splash.notifyProgress(null, "Loading Map Data...")
        GeoJSONManager.init()
        splash.notifyProgress(1.0, "Done Loading Map Data!")
        splash.notifyProgress(null, "Loading Colormap Data...")
        ColormapManager.init()
        splash.notifyProgress(1.0, "Done Loading Colormap Data!")

        AppState.radarDataProvider.addProgressListener(splash)
        AppState.radarDataProvider.setup()
        AppState.radarDataService.addProgressListener(splash)
        AppState.radarDataService.init()

        AppState.init(splash)

        launch(Dispatchers.Swing) {
            splash.isVisible = false

            if(SystemInfo.isLinux) {
                JFrame.setDefaultLookAndFeelDecorated(true);
                JDialog.setDefaultLookAndFeelDecorated(true);
            }


            val window = AppWindow()
            window.isVisible = true
        }
    }
}
