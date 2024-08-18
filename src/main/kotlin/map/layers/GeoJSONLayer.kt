package map.layers

import org.json.JSONArray
import org.json.JSONObject
import org.lwjgl.opengl.GL45.GL_FLOAT
import org.lwjgl.opengl.GL45.GL_STATIC_DRAW
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File

class GeoJSONLayer(val json: JSONObject) : MapLayer {
    private lateinit var vbo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var shader: ShaderProgram

    override fun init(camera: Camera) {
        val vertices = arrayListOf<FloatArray>()
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
            //vertices.map { vert -> println(vert.contentToString()) }
        } else {
            println("Unsupported GeoJSON root type!")
        }
    }

    fun parsePolygon(coordsArr: JSONArray, vertices: ArrayList<FloatArray>) {
        for (i in 0..<coordsArr.length()) {
            val innerPoly = coordsArr.getJSONArray(i)
            for (j in 0..<innerPoly.length()) {
                val coord = innerPoly.getJSONArray(j)

                val lon = coord.getFloat(0)
                val lat = coord.getFloat(1)
                vertices.add(
                    floatArrayOf(
                        lat, lon
                    )
                )

                if (j < innerPoly.length() - 1) {
                    val nextCoord = innerPoly.getJSONArray(j + 1)
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

        //initGraphics()
    }

    private fun initGraphics() {
        val vsSource = File("src/main/resources/shaders/lines/lines.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/lines/lines.fs.glsl").readText(Charsets.UTF_8)

        shader = ShaderProgram()
        shader.createVertexShader(vsSource)
        shader.createFragmentShader(fsSource)
        shader.link()

        val verts = MemoryUtil.memAllocFloat(7 * 3)

        vao = VertexArrayObject()
        vao.bind()

        vbo = GLBufferObject()
        vbo.bind()
        vbo.uploadData(verts, GL_STATIC_DRAW)

        MemoryUtil.memFree(verts)

//        vertexArrayObject.attrib(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
//        vertexArrayObject.attrib(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
        vao.attrib(0, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (1 * Float.SIZE_BYTES).toLong())
        vao.attrib(2, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
    }

    override fun render(camera: Camera) {
        TODO("Not yet implemented")
    }

    override fun destroy() {
        //TODO: Destroy any allocated opengl objects and other stuff
    }

}