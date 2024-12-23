import com.formdev.flatlaf.FlatDarkLaf
import data.radar.RadarDataProvider
import data.resources.GeoJSONManager
import data.state.AppState
import map.layers.GeoJSONLayer
import ucar.nc2.NetcdfFiles
import views.AppWindow
import views.SplashWindow
import javax.swing.SwingUtilities

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val splash = SplashWindow()
    splash.isVisible = true

    splash.onProgress(null, "Loading Map Data...")
    GeoJSONManager.init()
    splash.onProgress(1.0, "Done!")

    AppState.radarDataService.addProgressListener(splash)
    AppState.radarDataService.fillRepository()
    AppState.init()
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true

}