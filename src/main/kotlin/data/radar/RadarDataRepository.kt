package data.radar

import data.state.AppState
import kotlinx.coroutines.*
import meteo.radar.RadarVolume
import meteo.radar.VolumeFileHandle
import utils.CircularBuffer
import utils.ProgressListener

object RadarDataRepository {
    private var dataFiles: CircularBuffer<RadarVolume> = CircularBuffer(AppState.numLoopFrames.value)
    private val scope = MainScope()

    suspend fun loadInitialData(numFrames: Int, radarDataService: RadarDataService, progressListener: ProgressListener? = null) {
        val newBuffer = CircularBuffer<RadarVolume>(numFrames)
        if(progressListener != null)
            radarDataService.addProgressListener(progressListener)

        radarDataService.init(numFrames)

        val toDownload = radarDataService.pollRemaining()
        val volumes = arrayOfNulls<RadarVolume>(numFrames)
        val tasks = mutableListOf<Deferred<Unit>>()

        for(i in toDownload.indices) {
            if(toDownload[i] == null) continue
            tasks.add(scope.async(Dispatchers.IO) {
                val file = radarDataService.getFile(toDownload[i])
                if(file != null) {
                    volumes[i] = RadarVolume(file, toDownload[i])
                    file.close()

                    val progress = volumes.filterNotNull().size / volumes.size.toDouble()
                    progressListener?.notifyProgress(progress, "Loaded file ${volumes.filterNotNull().size}/${volumes.size}")
                }
            })
        }

        tasks.awaitAll()

        for(volume in volumes) {
            if(volume != null)
                newBuffer.add(volume)
        }

        setDataFiles(newBuffer)
    }

    fun clear() {
        dataFiles.clear()
    }

    fun setDataFiles(buffer: CircularBuffer<RadarVolume>) {
        this.dataFiles = buffer
    }

    fun setDataFiles(dataFiles: Iterable<RadarVolume>) {
        clear()
        for(file in dataFiles) {
            println("Adding file ${file.handle}...")
            addDataFile(file)
        }
    }

    fun addDataFile(file: RadarVolume) {
        dataFiles.add(file)
        println(dataFiles.getList().map { it.handle.fileName })
    }

    fun containsFile(handle: VolumeFileHandle): Boolean {
        dataFiles.forEach {
           if(it.handle == handle) return true
        }
        return false
    }

    fun lastFile(): RadarVolume? {
        return dataFiles.lastOrNull()
    }

    fun get(index: Int): RadarVolume? {
        return dataFiles.getList().getOrNull(index)
    }
}