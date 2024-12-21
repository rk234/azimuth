package data.radar

import data.state.AppState

class RadarDataService(private val station: String, var autoPoll: Boolean, val provider: RadarDataProvider) : Runnable {

    fun fillFrames() {
        val fileList = provider.getDataFileList(station)
        fileList.slice((fileList.size-1- AppState.numLoopFrames.value)..<fileList.size)
        for(file in fileList) {

        }
    }

    override fun run() {
        while(autoPoll) {

        }
    }


    fun stop() {
        autoPoll = false
    }
}