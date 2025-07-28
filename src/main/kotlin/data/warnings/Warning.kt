package data.warnings

import map.geometry.GeoLatLon
import map.geometry.GeoPolygon
import org.json.JSONException
import org.json.JSONObject
import java.time.ZonedDateTime

enum class WarningType {
    SEVERE_THUNDERSTORM,
    TORNADO,
    FLASH_FLOOD,
    SNOW_SQUALL,
    SPECIAL_MARINE,
    SPECIAL_WEATHER_STATEMENT,
    SEVERE_WEATHER_STATEMENT;

    override fun toString(): String {
        return when (this) {
            SEVERE_THUNDERSTORM -> "SVR: Severe Thunderstorm"
            TORNADO -> "TOR: Tornado"
            FLASH_FLOOD -> "FFW: Flash Flood Warning"
            SNOW_SQUALL -> "SQW: Snow Squall Warning"
            SPECIAL_MARINE -> "SMW: Special Marine Warning"
            SPECIAL_WEATHER_STATEMENT -> "SPS: Special Weather Statement"
            SEVERE_WEATHER_STATEMENT -> "SVS: Severe Weather Statement"
        }
    }

    companion object {
        fun fromEventCode(event: String): WarningType? {
            return when (event) {
                "TOR" -> TORNADO
                "SVR" -> SEVERE_THUNDERSTORM
                "FFW" -> FLASH_FLOOD
                "SMW" -> SPECIAL_MARINE
                "SQW" -> SNOW_SQUALL
                "SPS" -> SPECIAL_WEATHER_STATEMENT
                "SVS" -> SEVERE_WEATHER_STATEMENT
                else -> null // Unknown event code
            }
        }
    }
}

data class Warning(
    val id: String,
    val name: String,
    val headline: String,
    val type: WarningType?,
    val message: String,
    val areaDesc: String,
    val sent: ZonedDateTime,
    val effective: ZonedDateTime,
    val onset: ZonedDateTime,
    val expires: ZonedDateTime,
    val polygons: List<GeoPolygon>
) {
    override fun toString(): String {
        return "Warning(name='$name', type=$type, message='$message', areaDesc='$areaDesc', sent=$sent, effective=$effective, onset=$onset, expires=$expires, polygons=$polygons)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Warning) return false

        return other.id == id
    }

    companion object {
        fun fromJson(json: JSONObject): Warning {
            val geoPolygons = mutableListOf<GeoPolygon>()

            try {
                val polygons = json.getJSONObject("geometry").getJSONArray("coordinates")
                for (i in 0 until polygons.length()) {
                    val polygonCoords = polygons.getJSONArray(i)
                    val coordinates = mutableListOf<GeoLatLon>()
                    for (j in 0 until polygonCoords.length()) {
                        val coord = polygonCoords.getJSONArray(j)
                        coordinates.add(
                            GeoLatLon(
                                lat = coord.getDouble(1).toFloat(),
                                lon = coord.getDouble(0).toFloat()
                            )
                        )
                    }
                    geoPolygons.add(GeoPolygon(coordinates))
                }
            } catch (e: JSONException) {
                println("Error parsing geometry coordinates: ${e.message}")
            }

            val properties = json.getJSONObject("properties") ?: throw JSONException("Missing properties in JSON")
            val id = properties.getString("id")
            val areaDesc = properties.getString("areaDesc") ?: "No area description provided"
            val sent = ZonedDateTime.parse(properties.getString("sent"))
            val effective = ZonedDateTime.parse(properties.getString("effective"))
            val onset = ZonedDateTime.parse(properties.getString("onset"))
            val expires = ZonedDateTime.parse(properties.getString("expires"))
            val type = properties.getString("event")
            val description = properties.getString("description")
            val headline = properties.getString("headline")

            val eventCode = try {
                properties.getJSONObject("eventCode").getJSONArray("SAME").getString(0)
            } catch (e: JSONException) {
                println("Error parsing event code: ${e.message}")
                null
            }

            return Warning(
                id = id,
                name = type,
                headline = headline,
                message = description,
                areaDesc = areaDesc,
                sent = sent,
                effective = effective,
                onset = onset,
                expires = expires,
                polygons = geoPolygons,
                type = if(eventCode != null) WarningType.fromEventCode(eventCode) else null
            )
        }
    }
}
