package data.state

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.radar.RadarDataService
import meteo.radar.*
import ucar.nc2.NetcdfFiles

object AppState {
    var radarDataProvider = RadarDataProvider()
    var activeVolume: State<RadarVolume?> = State(null)
    var numLoopFrames = State(UserPrefs.numLoopFrames())
    var activeStation = State("KLGX")
    var radarDataService = RadarDataService(activeStation.value, true, radarDataProvider)

    fun init() {
        println(RadarDataRepository.dataFiles.getList())
        println(RadarDataRepository.lastFile())
        activeVolume.value = RadarDataRepository.lastFile()?.second
    }
}

