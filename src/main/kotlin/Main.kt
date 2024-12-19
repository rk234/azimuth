import com.formdev.flatlaf.FlatDarkLaf
import data.RadarDataProvider
import data.ShaderManager
import meteo.radar.Product
import meteo.radar.RadarProductVolume
import org.lwjgl.opengl.GL
import ucar.nc2.NetcdfFiles
import utils.ProgressListener
import views.AppWindow
import views.RadarProductPane
import views.SideBar
import views.SplashWindow
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.SplashScreen
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val splash = SplashWindow()
    splash.isVisible = true

    val radarDataProvider = RadarDataProvider()
    radarDataProvider.addProgressListener(splash)
    radarDataProvider.init()

    splash.onProgress(0.0, "Loading radar file...")

    val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
//    val vol = RadarProductVolume(file, Product.REFLECTIVITY_HIRES)
    splash.onProgress(1.0, "Done loading data file!")
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true

}