package data.radar

import data.PollableDataService
import data.state.AppState
import meteo.radar.RadarVolume
import meteo.radar.VolumeFileHandle
import utils.ProgressListener
import java.util.*

class RadarDataService(private val station: String, val provider: RadarDataProvider) :
    PollableDataService<VolumeFileHandle> {
    private val progressListeners: MutableList<ProgressListener> = mutableListOf()
    private val pollQueue: Queue<VolumeFileHandle> = LinkedList()
    private var lastPoll: VolumeFileHandle? = null

    fun addProgressListener(listener: ProgressListener) {
        this.progressListeners.add(listener)
    }

    private fun notifyProgress(message: String, progress: Double? = null) {
        for (listener in progressListeners) {
            listener.onProgress(progress, message)
        }
    }

    override fun init() {
        notifyProgress("Loading radar data for $station...")
        var fileList = provider.getDataFileList(station)
        notifyProgress("Fetched data file list for $station")

        fileList = fileList.slice((fileList.size- AppState.numLoopFrames.value)..<fileList.size)
        for(fileName in fileList) {
            pollQueue.offer(fileName)
        }
    }

    fun peek(): VolumeFileHandle? {
        return pollQueue.peek()
    }

    override fun poll(): VolumeFileHandle? {
        try {
            if(pollQueue.isEmpty()) {
                val files = provider.getDataFileList(station)
                val latest = files.last()
                if(latest != lastPoll)
                    pollQueue.offer(latest)
            }

            if(!pollQueue.isEmpty()) {
                val toFetch = pollQueue.poll()
                lastPoll = toFetch
                return toFetch
            }

        } catch (ex: Exception) {
            println("radar service poll error:")
            println(ex)
        }

        return null
    }
}