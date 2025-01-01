package data.radar

import data.state.AppState
import data.state.UserPrefs
import meteo.radar.RadarVolume
import meteo.radar.VolumeFileHandle
import ucar.nc2.NetcdfFile
import utils.CircularBuffer

object RadarDataRepository {
    var dataFiles: CircularBuffer<RadarVolume> = CircularBuffer(AppState.numLoopFrames.value)

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