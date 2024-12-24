package data.state

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.radar.RadarDataService
import meteo.radar.*
import ucar.nc2.NetcdfFiles
import kotlin.concurrent.thread

object AppState {
    var radarDataProvider = RadarDataProvider()
    var activeVolume: State<RadarVolume?> = State(null)
    var numLoopFrames = State(UserPrefs.numLoopFrames())
    var activeStation = State(UserPrefs.defaultStation())
    var radarDataService = RadarDataService(activeStation.value, radarDataProvider)

    fun init() {
        loadInitialData()
        activeVolume.value = RadarDataRepository.lastFile()
    }

    fun pollRadarData() {
        val handle = radarDataService.poll() ?: return
        println("Found new radar data!")
        val file = radarDataProvider.getDataFile(handle)
        if(file != null) {
            println("downloaded new radar data!")
            RadarDataRepository.addDataFile(RadarVolume(file, handle))
        } else {
            println("could not find new radar data!")
        }
    }

    private fun loadInitialData() {
        var nextToPoll: VolumeFileHandle?
        val toDownload = arrayOfNulls<VolumeFileHandle>(numLoopFrames.value)
        var index = 0
        do {
            nextToPoll = radarDataService.peek()

            if(nextToPoll != null) {
                toDownload[index] = radarDataService.poll()
                println("Fetched file ${toDownload[index]}")
                index++
            }
        } while(nextToPoll != null)

        val volumes = arrayOfNulls<RadarVolume>(numLoopFrames.value)
        val threads = mutableListOf<Thread>()
        for(i in toDownload.indices) {
            if(toDownload[i] == null) continue
            threads.add(thread {
                val file = radarDataProvider.getDataFile(toDownload[i]!!)
                if(file != null)
                    volumes[i] = RadarVolume(file, toDownload[i]!!)
            })
        }

        for(thread in threads) {
            thread.join()
        }

        for(volume in volumes) {
            if(volume != null)
                RadarDataRepository.addDataFile(volume)
        }
    }
}

