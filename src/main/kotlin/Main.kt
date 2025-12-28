import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.util.SystemInfo
import data.radar.RadarCache
import data.resources.ColormapManager
import data.resources.GeoJSONManager
import data.state.AppState
import data.state.UserPrefs
import kotlinx.coroutines.*
import kotlinx.coroutines.swing.Swing
import rendering.FontAtlas
import views.AppWindow
import views.SplashWindow
import java.awt.Font
import javax.swing.JDialog
import javax.swing.JFrame

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")

    val atlas = FontAtlas("src/main/resources/fonts/Inter/static/Inter_24pt-Regular.ttf", 32f, 1024, 1024)
    atlas.writeBitmapToFile("font_atlas.png")

    // This resets user prefs to defaults, remove when not in dev
    UserPrefs.reset()

    RadarCache.init(UserPrefs.radarCachePath())

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
