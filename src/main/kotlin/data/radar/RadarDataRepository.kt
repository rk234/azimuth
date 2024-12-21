package data.radar

import data.state.UserPrefs
import ucar.nc2.NetcdfFile
import utils.CircularBuffer

object RadarDataRepository {
    var dataFiles: CircularBuffer<NetcdfFile> = CircularBuffer(UserPrefs.numLoopFrames())

    fun addDataFile(file: NetcdfFile) {
        dataFiles.add(file)
    }
}