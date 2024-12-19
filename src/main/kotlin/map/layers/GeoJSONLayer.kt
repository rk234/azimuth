package map.layers

import data.ShaderManager
import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector3f
import org.json.JSONArray
import org.json.JSONObject
import rendering.*
import java.io.File

class GeoJSONLayer(private val json: JSONObject, val lineWidth: Float, val lineColor: Vector3f) : MapLayer {
    private lateinit var shader: ShaderProgram
    private var paths: ArrayList<PathRenderable> = ArrayList()
    private val vertsPerChunk = 60_000

    override fun init(camera: Camera) {
        val vertices = arrayListOf<Vector2f>()
        if (json.getString("type") == "FeatureCollection") {
            val features = json.getJSONArray("features")

            for (i in 0..<features.length()) {
                val feature = features.getJSONObject(i)
                if (feature.getString("type") == "Feature") {
                    val geometry = feature.getJSONObject("geometry")
                    val geometryType = geometry.getString("type")
                    val coordinates = geometry.getJSONArray("coordinates")

                    when (geometryType) {
                        "MultiPolygon" -> {
                            for (j in 0..<coordinates.length()) {
                                parsePolygon(coordinates.getJSONArray(j), vertices)
                            }
                        }

                        "Polygon" -> {
                            parsePolygon(coordinates, vertices)
                        }
                    }
                }
            }

            initGraphics(vertices)

            val chunks = vertices.chunked(vertsPerChunk)
            chunks.forEach { c ->
                val renderable = PathRenderable(c, shader, lineWidth, lineColor)
                paths.add(renderable)
                renderable.init()
            }
        } else {
            println("Unsupported GeoJSON root type!")
        }
    }

    private fun parsePolygon(coordsArr: JSONArray, vertices: ArrayList<Vector2f>) {
        for (i in 0..<coordsArr.length()) {
            val innerPoly = coordsArr.getJSONArray(i)
            for (j in 0..<innerPoly.length()) {
                val coord = innerPoly.getJSONArray(j)
                val proj = MercatorProjection()

                val lon = coord.getFloat(0)
                val lat = coord.getFloat(1)

                val p1 = proj.toCartesian(
                    Vector2f(
                        lat, lon
                    )
                )
                var p2: Vector2f


                if (j < innerPoly.length() - 1) {
                    val nextCoord = innerPoly.getJSONArray(j + 1)
                    val nextLon = nextCoord.getFloat(0)
                    val nextLat = nextCoord.getFloat(1)

                    p2 = proj.toCartesian(
                        Vector2f(
                            nextLat, nextLon
                        )
                    )
                } else {
                    val nextCoord = innerPoly.getJSONArray(0)
                    val nextLon = nextCoord.getFloat(0)
                    val nextLat = nextCoord.getFloat(1)

                    p2 = proj.toCartesian(
                        Vector2f(
                            nextLat, nextLon
                        )
                    )
                }

                vertices.add(p1)
                vertices.add(p2)
            }
        }


    }

    private fun initGraphics(vertices: ArrayList<Vector2f>) {
        shader = ShaderManager.instance.linesShader()
    }

    override fun render(camera: Camera) {
        for (path in paths) {
            path.draw(camera)
        }
    }

    override fun destroy() {
        for (path in paths) {
            path.destroy()
        }
        shader.destroy()
    }

}