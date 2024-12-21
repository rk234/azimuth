import com.formdev.flatlaf.FlatDarkLaf
import data.radar.RadarDataProvider
import ucar.nc2.NetcdfFiles
import views.AppWindow
import views.SplashWindow

fun main() {
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    System.setProperty("apple.awt.application.appearance", "system")
    FlatDarkLaf.setup()

    val splash = SplashWindow()
    splash.isVisible = true

    splash.onProgress(0.0, "Loading radar file...")

    val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
//    val vol = RadarProductVolume(file, Product.REFLECTIVITY_HIRES)
    splash.onProgress(1.0, "Done loading data file!")
    splash.isVisible = false

    val window = AppWindow()
    window.isVisible = true
}