package data.resources

import org.json.JSONObject
import java.io.File

class GeoJSONManager {
    companion object {
        val instance = GeoJSONManager()

        fun init() {
            instance.loadAll()
        }
    }

    lateinit var countries: JSONObject
    lateinit var states: JSONObject
    lateinit var counties: JSONObject

    fun loadAll() {
        loadCountries()
        loadStates()
        loadCounties()
    }

    fun loadCountries() {
        countries = JSONObject(
            File("src/main/resources/geo/countries.geojson").readText(Charsets.UTF_8)
        )
    }

    fun loadStates() {
        states = JSONObject(
            File("src/main/resources/geo/states.geojson").readText(Charsets.UTF_8)
        )
    }

    fun loadCounties() {
        counties = JSONObject(
            File("src/main/resources/geo/counties.json").readText(Charsets.UTF_8)
        )
    }
}