package data.radar

import data.PollableDataService
import meteo.radar.VolumeFileHandle
import ucar.nc2.NetcdfFile
import utils.ProgressListener
import java.util.*

class RadarDataService(private val station: String, private val provider: RadarDataProvider) :
    PollableDataService<VolumeFileHandle> {
    private val progressListeners: MutableSet<ProgressListener> = mutableSetOf()
    private val pollQueue: Queue<VolumeFileHandle> = LinkedList()
    private var lastPoll: VolumeFileHandle? = null

    fun addProgressListener(listener: ProgressListener) {
        this.progressListeners.add(listener)
    }

    private fun notifyProgress(message: String, progress: Double? = null) {
        for (listener in progressListeners) {
            listener.notifyProgress(progress, message)
        }
    }

    fun getFile(handle: VolumeFileHandle): NetcdfFile? {
        return provider.getDataFile(handle)
    }

    override fun init(numInitialData: Int) {
        pollQueue.clear()
        lastPoll = null;

        notifyProgress("Loading radar data for $station...")
        var fileList = provider.getDataFileList(station)
        notifyProgress("Fetched data file list for $station")

        fileList = fileList.slice((fileList.size-numInitialData)..<fileList.size)
        for(fileName in fileList) {
            pollQueue.offer(fileName)
        }
    }

    fun pollRemaining(): List<VolumeFileHandle> {
        val remaining = mutableListOf<VolumeFileHandle>()
        while(!pollQueue.isEmpty()) {
            val next = poll() ?: break
            remaining.add(next)
        }
        return remaining
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