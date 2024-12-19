package data

import data.AppState.activeVolume
import meteo.radar.Product
import meteo.radar.RadarProductVolume
import meteo.radar.RadarScan
import meteo.radar.Station
import ucar.nc2.NetcdfFiles

object AppState {
    var activeVolume: RadarProductVolume = RadarProductVolume(NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921"), Product.REFLECTIVITY_HIRES)
    var station: Station = activeVolume.station

    fun getScan(tilt: Int, product: Product): RadarScan {
        TODO()
    }
}

