package data.radar

import data.state.AppState
import meteo.radar.RadarVolume
import meteo.radar.VolumeFileHandle
import utils.CircularBuffer

object RadarDataRepository {
    var dataFiles: CircularBuffer<RadarVolume?> = CircularBuffer(AppState.numLoopFrames.value)

    fun resize(newCapacity: Int) {
        val newBuffer = CircularBuffer<RadarVolume?>(newCapacity)
        if(newCapacity > dataFiles.capacity) {
            for(i in 0..<newCapacity-dataFiles.capacity) {
                newBuffer.add(null)
            }

            for(i in 0..<dataFiles.capacity) {
                newBuffer.add(dataFiles.get(i))
            }
        } else if(newCapacity < dataFiles.capacity) {
            for(i in dataFiles.capacity - newCapacity..<dataFiles.capacity) {
                newBuffer.add(dataFiles.get(i))
            }
        }
        dataFiles = newBuffer
    }

    fun clear() {
        dataFiles.clear()
    }

    fun setDataFiles(dataFiles: Iterable<RadarVolume>) {
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