package data.radar

import data.state.AppState
import data.state.UserPrefs
import meteo.radar.RadarVolume
import utils.CircularBuffer
import utils.ProgressListener

class RadarDataService(private val station: String, var autoPoll: Boolean, val provider: RadarDataProvider) : Runnable {
    private val progressListeners: MutableList<ProgressListener> = mutableListOf()

    fun addProgressListener(listener: ProgressListener) {
        this.progressListeners.add(listener)
    }

    private fun notifyProgress(message: String, progress: Double? = null) {
        for (listener in progressListeners) {
            listener.onProgress(progress, message)
        }
    }

    fun fillRepository() {
        notifyProgress("Loading radar data for $station...")
        var fileList = provider.getDataFileList(station)
        notifyProgress("Fetched data file list for $station")

        fileList = fileList.slice((fileList.size- AppState.numLoopFrames.value)..<fileList.size)
        val buf = CircularBuffer<Pair<String, RadarVolume>>(AppState.numLoopFrames.value)

        notifyProgress("Fetching data files...", 0.0)
        for((index, file) in fileList.withIndex()) {
            if(!RadarDataRepository.containsFile(file)) {
                notifyProgress("Downloading file ${index+1} of ${AppState.numLoopFrames.value}...", (index+1).toDouble() / AppState.numLoopFrames.value)
                val dataFile = provider.getDataFile(file)
                if(dataFile != null) {
                    buf.add(
                        Pair(file, RadarVolume(dataFile))
                    )
                    notifyProgress("Fetched file ${index+1} of ${AppState.numLoopFrames.value}...", (index+1).toDouble() / AppState.numLoopFrames.value)
                }
            }
        }
        notifyProgress("Done fetching radar data!",1.0)
        RadarDataRepository.setDataFiles(buf)
    }

    override fun run() {
        while(autoPoll) {
            try {
                val files = provider.getDataFileList(station)
                val latest = files.last()

                if(!RadarDataRepository.containsFile(latest)) {
                    val file = provider.getDataFile(latest)
                    if(file != null) {
                        RadarDataRepository.addDataFile(latest, RadarVolume(file))
                    } else {
                        println("auto poll error")
                    }
                }
                Thread.sleep(UserPrefs.autoPollFrequency())
            } catch (e: Exception) {
                println("auto poll error")
            }
        }
    }


    fun stop() {
        autoPoll = false
    }
}