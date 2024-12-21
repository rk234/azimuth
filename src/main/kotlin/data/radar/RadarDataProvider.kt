package data.radar

import okhttp3.OkHttpClient
import okhttp3.Request
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFiles
import utils.ProgressListener

class RadarDataProvider {
    private var progressListeners: ArrayList<ProgressListener> = ArrayList()
    private val httpClient = OkHttpClient()
    private val radarServiceURL = "https://mesonet-nexrad.agron.iastate.edu/level2/raw"
    private val listFile: String

    init {
        val req = Request.Builder().url("$radarServiceURL/config.cfg").build()

        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) throw Exception("Failed to load station list, received response code: ${resp.code}")
        val config = resp.body?.string() ?: ""
        val listFileLine = config.split("\n").getOrNull(0)

        if(listFileLine == null) throw Exception("Failed to find list file!")
        listFile = listFileLine.substringAfter("ListFile: ")
    }

    fun addProgressListener(listener: ProgressListener) {
        progressListeners.add(listener)
    }

    fun setProgressListeners(listeners: List<ProgressListener>) {
        progressListeners = ArrayList(listeners)
    }

    fun getStationList(): List<String> {
        val stations = ArrayList<String>()
        reportProgress(null, "Loading station list...")
        val req = Request.Builder().url("$radarServiceURL/config.cfg").build()

        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) throw Exception("Failed to load station list, received response code: ${resp.code}")
        val config = resp.body?.string() ?: ""
        val lines = config.split("\n")

        if(lines.size < 2) throw Exception("Failed to parse station list!")

        reportProgress(null, "Parsing Station List...")

        for(i in 1..<lines.size) {
            val stationLine = lines[i]
            if(stationLine.startsWith("Site: ")) {
                val station = stationLine.substringAfter("Site: ")
                stations.add(station)
            }
        }

        reportProgress(1.0, "Finished loading station list!")

        return stations
    }


    fun getDataFileList(station: String): List<String> {
        val files = ArrayList<String>()
        val req = Request.Builder().url("$radarServiceURL/$station/$listFile").build()
        val res = httpClient.newCall(req).execute()
        if(!res.isSuccessful) throw Exception("Failed to load data file list, received response code: ${res.code}")

        val lines = res.body?.string()?.split("\n") ?: emptyList()

        for(line in lines) {
            val file = line.split(" ").getOrNull(1)
            if(file != null) files.add(file)
        }

        return files
    }

    fun getDataFile(name: String): NetcdfFile? {
        val station = name.substringBefore("_")

        val req = Request.Builder().url("$radarServiceURL/$station/$name").build()
        val resp = httpClient.newCall(req).execute()
        if(!resp.isSuccessful) throw Exception("Failed to load data file, received response code: ${resp.code}")

        val bytes = resp.body?.bytes() ?: return null

        return NetcdfFiles.openInMemory(name, bytes)
    }

    private fun reportProgress(progress: Double?, message: String) {
        progressListeners.forEach { it.onProgress(progress, message) }
    }

}