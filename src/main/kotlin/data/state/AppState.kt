package data.state

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.radar.RadarDataService
import kotlinx.coroutines.*
import meteo.radar.*
import utils.ProgressListener

object AppState {
    var radarDataProvider = RadarDataProvider()
    var activeVolume: State<RadarVolume?> = State(null)
    var numLoopFrames = State(UserPrefs.numLoopFrames())
    var activeStation = State(UserPrefs.defaultStation())
    var radarDataService = RadarDataService(activeStation.value, radarDataProvider)

    suspend fun init(progressListener: ProgressListener? = null) {
        radarDataProvider.setup()
        loadInitialData(progressListener)
        activeVolume.value = RadarDataRepository.lastFile()

        activeStation.onChange {
            radarDataService = RadarDataService(it, radarDataProvider)
        }
    }

    private suspend fun loadInitialData(progressListener: ProgressListener? = null) = coroutineScope {
        progressListener?.notifyProgress(null, "Loading initial data for ${activeStation.value}")
        RadarDataRepository.loadInitialData(numLoopFrames.value, radarDataService, progressListener)
    }
}

