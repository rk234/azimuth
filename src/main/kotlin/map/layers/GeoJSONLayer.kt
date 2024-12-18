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
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.Vector

class GeoJSONLayer(val json: JSONObject, val lineWidth: Float) : MapLayer {
    private lateinit var pathVBO: GLBufferObject
    private lateinit var prevVBO: GLBufferObject
    private lateinit var nextVBO: GLBufferObject
    private lateinit var dirVBO: GLBufferObject

    private lateinit var ibo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var shader: ShaderProgram
    private var numVerts: Int = 0

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
        val vsSource = File("src/main/resources/shaders/lines/lines.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/lines/lines.fs.glsl").readText(Charsets.UTF_8)

        shader = ShaderProgram()
        shader.createVertexShader(vsSource)
        shader.createFragmentShader(fsSource)
        shader.link()

        val verts = MemoryUtil.memAllocFloat((vertices.size * 2)*2)
        val next = MemoryUtil.memAllocFloat((vertices.size * 2)*2)
        val prev = MemoryUtil.memAllocFloat((vertices.size * 2)*2)
        val dirs = MemoryUtil.memAllocFloat((vertices.size * 2))

        pack(duplicate(vertices), verts)
        pack(duplicate(shiftVerts(vertices, +1)), next)
        pack(duplicate(shiftVerts(vertices, -1)), prev)

        vertices.forEach { _ ->
            dirs.put(1.0f)
            dirs.put(-1.0f)
        }

        println("GEOJSON Verts ${verts.capacity()}")
//        println(vertices)

        verts.flip()
        next.flip()
        prev.flip()
        dirs.flip()

        numVerts = (vertices.size - 1)*6

        vao = VertexArrayObject()
        vao.bind()

        pathVBO = GLBufferObject()
        pathVBO.bind()
        pathVBO.uploadData(verts, GL_STATIC_DRAW)

        prevVBO = GLBufferObject()
        prevVBO.bind()
        prevVBO.uploadData(prev, GL_STATIC_DRAW)

        nextVBO = GLBufferObject()
        nextVBO.bind()
        nextVBO.uploadData(next, GL_STATIC_DRAW)

        dirVBO = GLBufferObject()
        dirVBO.bind()
        dirVBO.uploadData(dirs, GL_STATIC_DRAW)

        ibo = GLBufferObject()

        val indices = MemoryUtil.memAllocInt((vertices.size) * 6)
        generateIndices(indices, vertices.size)
        for(i in 0..<indices.capacity()) {
            println(indices.get(i))
        }
//        indices.flip()

        MemoryUtil.memFree(verts)
        MemoryUtil.memFree(prev)
        MemoryUtil.memFree(next)
        MemoryUtil.memFree(indices)

        pathVBO.bind()
        vao.attrib(0, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(0)
        nextVBO.bind()
        vao.attrib(1, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(1)
        prevVBO.bind()
        vao.attrib(2, 2, GL_FLOAT, false, 2 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(2)
        dirVBO.bind()
        vao.attrib(3, 1, GL_FLOAT, false, 1 * Float.SIZE_BYTES, 0)
        vao.enableAttrib(3)
        ibo.bind(GL_ELEMENT_ARRAY_BUFFER)
        ibo.uploadData(indices, GL_STATIC_DRAW, GL_ELEMENT_ARRAY_BUFFER)
    }

    private fun pack(path: ArrayList<Vector2f>, buf: FloatBuffer) {
        for(vec in path) {
            buf.put(vec[0])
            buf.put(vec[1])
        }
    }

    private fun duplicate(path: ArrayList<Vector2f>): ArrayList<Vector2f> {
        val list = ArrayList<Vector2f>(path.size * 2)
        path.forEach { vert ->
            list.add(vert)
            list.add(vert)
        }
        return list
    }

    private fun shiftVerts(path: ArrayList<Vector2f>, offset: Int): ArrayList<Vector2f> {
        val list = ArrayList<Vector2f>(path.size)
        for(i in 0..<path.size) {
            list.add(path[clamp(i + offset, path.size - 1, 0)])
        }
        return list
    }

    private fun generateIndices(indices: IntBuffer, pathLen: Int) {
        var c = 0
        var index = 0
        for(j in 0..<pathLen) {
            val i = index
            indices.put(c++, i)
            indices.put(c++, i + 1)
            indices.put(c++, i + 2)
            indices.put(c++, i + 2)
            indices.put(c++, i + 1)
            indices.put(c++, i + 3)
            index+=4
        }
    }

    private fun shiftVerts(vertices: ArrayList<Vector2f>, offset: Int, out: FloatBuffer) {
        for(i in 0..<vertices.size) {
            out.put(vertices[clamp(i + offset, vertices.size - 1, 0)][0])
            out.put(vertices[clamp(i + offset, vertices.size - 1, 0)][1])
        }
    }

    private fun clamp(v: Int, max: Int, min: Int): Int {
        return if(v > max) max else if(v < min) min else v;
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
//        pathVBO.bind()
//        vao.enableAttrib(0)
//        nextVBO.bind()
//        vao.enableAttrib(1)
//        prevVBO.bind()
//        vao.enableAttrib(2)
//        dirVBO.bind()
//        vao.enableAttrib(3)
//        ibo.bind(GL_ELEMENT_ARRAY_BUFFER)

        glDrawElements(GL_TRIANGLES, numVerts, GL_UNSIGNED_INT, 0)
//        glDrawArrays(GL_TRIANGLES, 0, numVerts)
    }

    override fun destroy() {
        vao.destroy()
        pathVBO.destroy()
        shader.destroy()
    }

}