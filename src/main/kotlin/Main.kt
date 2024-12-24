import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.util.SystemInfo
import data.radar.RadarDataProvider
import data.resources.ColormapManager
import data.resources.ColormapTextureManager
import data.resources.GeoJSONManager
import data.state.AppState
import map.layers.GeoJSONLayer
import okhttp3.Dispatcher
import org.lwjgl.opengl.GL
import ucar.nc2.NetcdfFiles
import views.AppWindow
import views.SplashWindow
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
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
    AppState.radarDataService.fillRepository()
    AppState.init()
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true
}