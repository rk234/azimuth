package data.warnings

import data.PollableDataService
import map.projection.MercatorProjection
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class WarningDataService : PollableDataService<List<Warning>> {
    //event codes: https://www.weather.gov/nwr/eventcodes
    private val eventCodes = listOf(
        "TOR", // Tornado
        "SVR", // Severe Thunderstorm
        "FFW", // Flash Flood Warning
        "SMW", // Special Marine Warning
        "SQW", // Snow Squall Warning
        "SVS", // Severe Weather Statement
    )
    private val warningServiceURL = "https://api.weather.gov/alerts/active?status=actual&code=${eventCodes.joinToString(",")}"
    private val httpClient = OkHttpClient()

    override fun init(numInitialData: Int) {
        println("URL: $warningServiceURL")
    }

    override fun poll(): List<Warning>? {
        val req = Request.Builder().url(warningServiceURL).build()
        val resp = httpClient.newCall(req).execute()
        if (!resp.isSuccessful) {
            println("Failed to fetch warnings, received response code: ${resp.code}")
            return null
        }

        resp.use { response ->
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)
            val features = json.getJSONArray("features")

            if (features.isEmpty()) {
                println("No active warnings found.")
                return emptyList()
            }

            val warnings = mutableListOf<Warning>()
            for (i in 0 until features.length()) {
                try {
                    val warning = Warning.fromJson(features.getJSONObject(i))
                    warnings.add(warning)
                } catch (e: Exception) {
                    println("Failed to parse warning at index $i: ${e.message}")
                }
            }

            return warnings
        }
    }
}