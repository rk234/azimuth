package map.layers

import com.sun.jna.Memory
import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector3f
import org.json.JSONArray
import org.json.JSONObject
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File
import java.nio.IntBuffer

class GeoJSONLayer(val json: JSONObject, val lineWidth: Float) : MapLayer {
    private lateinit var pathVBO: GLBufferObject
    private lateinit var prevVBO: GLBufferObject
    private lateinit var nextVBO: GLBufferObject

    private lateinit var ibo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var shader: ShaderProgram
    private var numVerts: Int = 0

    //TODO: Duplication and index buffer
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
            //vertices.map { vert -> println(vert.contentToString()) }
            initGraphics(vertices)
        } else {
            println("Unsupported GeoJSON root type!")
        }
    }

    fun parsePolygon(coordsArr: JSONArray, vertices: ArrayList<Vector2f>) {
        for (i in 0..<coordsArr.length()) {
            val innerPoly = coordsArr.getJSONArray(i)
            for (j in 0..<innerPoly.length()) {
                val coord = innerPoly.getJSONArray(j)
                val proj = MercatorProjection()

                val lon = coord.getFloat(0)
                val lat = coord.getFloat(1)
                vertices.add(
                    proj.toCartesian(Vector2f(
                        lat, lon
                    ))
                )

                if (j < innerPoly.length() - 1) {
                    val nextCoord = innerPoly.getJSONArray(j + 1)
                    val nextLon = nextCoord.getFloat(0)
                    val nextLat = nextCoord.getFloat(1)

                    vertices.add(
                        proj.toCartesian(Vector2f(
                            nextLat, nextLon
                        ))
                    )
                    vertices.add(
                        proj.toCartesian(Vector2f(
                            nextLat, nextLon
                        ))
                    )
                } else {
                    val nextCoord = innerPoly.getJSONArray(0)
                    val nextLon = nextCoord.getFloat(0)
                    val nextLat = nextCoord.getFloat(1)

                    vertices.add(
                        proj.toCartesian(Vector2f(
                            nextLat, nextLon
                        ))
                    )
                    vertices.add(
                        proj.toCartesian(Vector2f(
                            nextLat, nextLon
                        ))
                    )
                }
                vertices.add(
                    proj.toCartesian(Vector2f(
                        lat, lon
                    ))
                )
            }
        }


    }

    private fun initGraphics(vertices: ArrayList<Vector2f>) {
        val vsSource = File("src/main/resources/shaders/lines/lines.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/lines/lines.fs.glsl").readText(Charsets.UTF_8)

        shader = ShaderProgram()
        shader.createVertexShader(vsSource)
        shader.createFragmentShader(fsSource)
        shader.link()

        val verts = MemoryUtil.memAllocFloat((vertices.size * 2)+4)
        println("GEOJSON Verts ${verts.capacity()}")

        // padding first value
        verts.put(vertices[0][0])
        verts.put(vertices[0][1])

        vertices.forEach { pt ->
            verts.put(pt[0])
            verts.put(pt[1])
        }

        // padding first value
        verts.put(vertices.last[0])
        verts.put(vertices.last[1])
        verts.flip()
        numVerts = verts.capacity() / 2


        vao = VertexArrayObject()
        vao.bind()

        pathVBO = GLBufferObject()
        pathVBO.bind()
        pathVBO.uploadData(verts, GL_STATIC_DRAW)

        ibo = GLBufferObject()
        ibo.bind(GL_ELEMENT_ARRAY_BUFFER)

        val indices = MemoryUtil.memAllocInt(vertices.size * 6)
        generateIndices(indices, vertices.size)
        ibo.uploadData(indices, GL_STATIC_DRAW, GL_ELEMENT_ARRAY_BUFFER)

        MemoryUtil.memFree(verts)
        MemoryUtil.memFree(indices)

        vao.attrib(0, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 2 * Float.SIZE_BYTES.toLong())
        vao.attrib(1, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 4 * Float.SIZE_BYTES.toLong())
        vao.attrib(2, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
    }

    private fun generateIndices(indices: IntBuffer, pathLen: Int) {
        var c = 0
        var index = 0
        for(j in 0..<pathLen) {
            val i = index
            indices.put(c++, i)
            indices.put(c++, i + 1)
            indices.put(c++, i + 2)
            indices.put(c++, i + 1)
            indices.put(c++, i + 3)
            index+=2
        }
    }

    override fun render(camera: Camera) {
        shader.bind()
        shader.setUniformMatrix4f("projection", camera.projectionMatrix)
        shader.setUniformMatrix4f("transform", camera.transformMatrix)
        shader.setUniformFloat("aspect", camera.viewportDims.y / camera.viewportDims.x)
        shader.setUniformVec2f("resolution", camera.viewportDims)
        shader.setUniformFloat("thickness", lineWidth)
        shader.setUniformInt("miter", 0)
        shader.setUniformVec3f("color", Vector3f(1.0f))

        vao.bind()
        pathVBO.bind()
        ibo.bind(GL_ELEMENT_ARRAY_BUFFER)
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        vao.enableAttrib(2)

        glDrawElements(GL_TRIANGLES, numVerts, GL_INT, 0)
//        glDrawArrays(GL_TRIANGLES, 0, numVerts)
    }

    override fun destroy() {
        vao.destroy()
        pathVBO.destroy()
        shader.destroy()
    }

}