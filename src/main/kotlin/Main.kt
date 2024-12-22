import com.formdev.flatlaf.FlatDarkLaf
import data.radar.RadarDataProvider
import data.state.AppState
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

    AppState.radarDataService.addProgressListener(splash)
    AppState.radarDataService.fillRepository()
    AppState.init()
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true

}