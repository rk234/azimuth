package data

import okhttp3.OkHttpClient
import okhttp3.Request
import utils.ProgressListener

class RadarDataProvider {
    private var progressListeners: ArrayList<ProgressListener> = ArrayList()
    private val httpClient = OkHttpClient()
    private val radarServiceURL = "https://mesonet-nexrad.agron.iastate.edu/level2/raw"
    lateinit var stationList: List<String>
    lateinit var listFile: String

    fun addProgressListener(listener: ProgressListener) {
        progressListeners.add(listener)
    }

    fun setProgressListeners(listeners: List<ProgressListener>) {
        progressListeners = ArrayList(listeners)
    }

    fun init() {
        val stations = ArrayList<String>()
        reportProgress(null, "Loading station list...")
        val req = Request.Builder().url("$radarServiceURL/config.cfg").build()

        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) throw Exception("Failed to load station list, received response code: ${resp.code}")
        val config = resp.body?.string() ?: ""
        val lines = config.split("\n")

        if(lines.size < 2) throw Exception("Failed to parse station list!")

        val dirListLine = lines[0]
        if(dirListLine.startsWith("ListFile: ")) {
            listFile = dirListLine.substringAfter("ListFile: ")
            reportProgress(null, "Acquired List File")
        } else {
            throw Exception("Failed to find list file!")
        }

        reportProgress(null, "Parsing Station List...")

        for(i in 1..<lines.size) {
            val stationLine = lines[i]
            if(stationLine.startsWith("Site: ")) {
                val station = stationLine.substringAfter("Site: ")
                stations.add(station)
            }
        }

        reportProgress(1.0, "Finished loading station list!")

        this.stationList = stations
    }

    private fun reportProgress(progress: Double?, message: String) {
        progressListeners.forEach { it.onProgress(progress, message) }
    }

}