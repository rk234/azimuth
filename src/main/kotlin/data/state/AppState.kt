package data.state

import data.radar.RadarDataProvider
import meteo.radar.*
import ucar.nc2.NetcdfFiles

object AppState {
    var activeVolume = State(RadarVolume(NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921")))
    var provider = RadarDataProvider()
//    var activeVolume = State(
//        RadarVolume(
//            provider.getDataFile(provider.getDataFileList("KLWX").dropLast(1).last())!!
//        )
//    )
    var numLoopFrames = State(UserPrefs.numLoopFrames())
    var activeStation = State(UserPrefs.defaultStation())
}

