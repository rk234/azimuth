package data.radar

import data.state.AppState
import data.state.UserPrefs
import meteo.radar.RadarVolume
import ucar.nc2.NetcdfFile
import utils.CircularBuffer

object RadarDataRepository {
    var dataFiles: CircularBuffer<Pair<String, RadarVolume>> = CircularBuffer(AppState.numLoopFrames.value)

    fun clear() {
        dataFiles.clear()
    }

    fun setDataFiles(dataFiles: Iterable<Pair<String, RadarVolume>>) {
        for(file in dataFiles) {
            println("Adding file $file...")
            addDataFile(file.first, file.second)
        }
    }

    fun addDataFile(name: String, file: RadarVolume) {
        dataFiles.add(Pair(name, file))
    }

    fun containsFile(name: String): Boolean {
        dataFiles.forEach {
           if(it.first == name) return true
        }
        return false
    }

    fun lastFile(): Pair<String, RadarVolume>? {
        return dataFiles.lastOrNull()
    }
}