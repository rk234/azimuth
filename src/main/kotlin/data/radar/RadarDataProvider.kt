package data.radar

import meteo.radar.VolumeFileHandle
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.notify
import ucar.nc2.NetcdfFile
import ucar.nc2.NetcdfFiles
import utils.ProgressListener

class RadarDataProvider {
    private var progressListeners: ArrayList<ProgressListener> = ArrayList()
    private val httpClient = OkHttpClient()
    private val radarServiceURL = "https://mesonet-nexrad.agron.iastate.edu/level2/raw"
    private lateinit var listFile: String

    fun setup() {
        val req = Request.Builder().url("$radarServiceURL/config.cfg").build()

        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) throw Exception("Failed to load station list, received response code: ${resp.code}")
        val config = resp.body?.string() ?: ""
        val listFileLine = config.split("\n").getOrNull(0) ?: throw Exception("Failed to find list file!")

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


    fun getDataFileList(station: String): List<VolumeFileHandle> {
        val files = ArrayList<VolumeFileHandle>()
        val req = Request.Builder().url("$radarServiceURL/$station/$listFile").build()

        reportProgress(null, "Downloading file list for ${station}...")
        val res = httpClient.newCall(req).execute()
        if(!res.isSuccessful) throw Exception("Failed to load data file list, received response code: ${res.code}")

        val lines = res.body?.string()?.split("\n") ?: emptyList()

        for(line in lines) {
            val file = line.split(" ").getOrNull(1)
            if(file != null) files.add(VolumeFileHandle(file))
        }


        reportProgress(1.0, "Done")
        return files.dropLast(1) // last file seems to be in the process of uploading,
                                    // finding out a better solution here would be a good idea
    }

    fun getDataFile(handle: VolumeFileHandle, progressListener: ProgressListener? = null): NetcdfFile? {
        val station = handle.station()

        val req = Request.Builder().url("$radarServiceURL/$station/$handle").build()

        reportProgress(null, "Downloading...")

        val resp = httpClient.newCall(req).execute()
        if(!resp.isSuccessful) return null

        val bytes = resp.body?.bytes() ?: return null

        reportProgress(null, "Opening...")
        val file = NetcdfFiles.openInMemory(handle.fileName, bytes)
        reportProgress(1.0, "Done")

        return file
    }

    private fun reportProgress(progress: Double?, message: String) {
        progressListeners.forEach { it.notifyProgress(progress, message) }
    }

}