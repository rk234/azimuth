package map

import org.json.JSONArray
import org.json.JSONObject
import rendering.Renderer

class GeoJSONLayer(val json: JSONObject) : MapLayer {

    override fun init() {
        val vertices = arrayListOf<FloatArray>()
        if(json.getString("type") == "FeatureCollection") {
            val features = json.getJSONArray("features")

            for(i in 0..<features.length()) {
                val feature = features.getJSONObject(i)
                if(feature.getString("type") == "Feature") {
                    val geometry = feature.getJSONObject("geometry")
                    val geometryType = geometry.getString("type")
                    val coordinates = geometry.getJSONArray("coordinates")

                    when(geometryType) {
                        "MultiPolygon" -> {
                            for(j in 0..<coordinates.length()) {
                                parsePolygon(coordinates.getJSONArray(j), vertices)
                            }
                        }
                        "Polygon" -> {
                            parsePolygon(coordinates, vertices)
                        }
                    }
                }
            }
            vertices.map { vert -> println(vert.contentToString()) }
        } else {
            println("Unsupported GeoJSON root type!")
        }
    }

    fun parsePolygon(coordsArr: JSONArray, vertices: ArrayList<FloatArray>) {
        for(i in 0..<coordsArr.length()) {
            val innerPoly = coordsArr.getJSONArray(i)
            for(j in 0..<innerPoly.length()) {
                val coord = innerPoly.getJSONArray(j)

                val lon = coord.getFloat(0)
                val lat = coord.getFloat(1)
                vertices.add(
                    floatArrayOf(
                        lat, lon
                    )
                )

                if(j < innerPoly.length()-1) {
                    val nextCoord = innerPoly.getJSONArray(j+1)
                    val nextLon = nextCoord.getFloat(0)
                    val nextLat = nextCoord.getFloat(1)

                    vertices.add(
                        floatArrayOf(
                            nextLat, nextLon
                        )
                    )
                }
            }
        }
    }

    override fun render(renderer: Renderer) {
        TODO("Not yet implemented")
    }

}