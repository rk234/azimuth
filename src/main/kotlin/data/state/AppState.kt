package data.state

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.radar.RadarDataService
import kotlinx.coroutines.*
import meteo.radar.*
import ucar.nc2.util.DiskCache

object AppState {
    var radarDataProvider = RadarDataProvider()
    var activeVolume: State<RadarVolume?> = State(null)
    var numLoopFrames = State(UserPrefs.numLoopFrames())
    var activeStation = State("KRTX")
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
            activeVolume.value = RadarDataRepository.lastFile()
        } else {
            println("could not find new radar data!")
        }
    }

    private fun loadInitialData() {
        runBlocking {
            var nextToPoll: VolumeFileHandle?
            val toDownload = arrayOfNulls<VolumeFileHandle>(numLoopFrames.value)
            var index = 0
            do {
                nextToPoll = radarDataService.peek()

                if(nextToPoll != null && index < numLoopFrames.value) {
                    toDownload[index] = radarDataService.poll()
                    println("Fetched file ${toDownload[index]}")
                    index++
                }
            } while(nextToPoll != null && index < numLoopFrames.value)

            val volumes = arrayOfNulls<RadarVolume>(numLoopFrames.value)
            val tasks = mutableListOf<Deferred<Unit>>()
            for(i in toDownload.indices) {
                if(toDownload[i] == null) continue
                tasks.add(async(Dispatchers.IO) {
                    val file = radarDataProvider.getDataFile(toDownload[i]!!)
                    if(file != null) {
                        volumes[i] = RadarVolume(file, toDownload[i]!!)
                        file.close()
                    }
                })
            }

            tasks.awaitAll()

            for(volume in volumes) {
                if(volume != null)
                    RadarDataRepository.addDataFile(volume)
            }
        }
    }
}

