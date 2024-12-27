package rendering

import data.resources.ColormapManager
import map.projection.MercatorProjection
import map.projection.aerToGeo
import meteo.radar.RadarGate
import meteo.radar.RadarSweep
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import java.nio.IntBuffer

class RadarScanRenderable(private val sweep: RadarSweep, private val radarShader: ShaderProgram, private val cmapTexture: Texture1D) : Renderable {
    private var gateCount: Int = 0
    private lateinit var vbo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var ibo: GLBufferObject
    private var initialized = false

    override fun init() {
        if(initialized) return;
        val verts = MemoryUtil.memAllocFloat(sweep.numRadials * sweep.numGates * 3 * 4)
        val proj = MercatorProjection()
        val cmap = ColormapManager.instance.getDefault(sweep.product)

        val resolution =
            360.0f / sweep.radials.size
        var gateSize: Float = sweep.gateWidth
        for ((radialIndex, radial) in sweep.radials.withIndex()) {
            for (gateIndex in 0..<radial.gates.capacity()) {
                val gate = RadarGate(radial.gates.get(gateIndex))
                val azimuth = radial.azimuth
                val range = sweep.rangeStart + (gateSize*gate.idx().toFloat()) // 1000
                val data = gate.scaledValue(sweep.scale, sweep.addOffset)

                val startAngle = (azimuth.toDouble()) - (resolution / 2) * 1.12f
                val endAngle = (azimuth.toDouble()) + (resolution / 2) * 1.12f

                val p1 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            sweep.elevation,
                            range,
                            sweep.station.latitude,
                            sweep.station.longitude,
                        )
                    )

                val p2 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            sweep.elevation,
                            range + gateSize,
                            sweep.station.latitude,
                            sweep.station.longitude,
                        )
                    )
                val p3 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            sweep.elevation,
                            range + gateSize,
                            sweep.station.latitude,
                            sweep.station.longitude,
                        )
                    )
                val p4 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            sweep.elevation,
                            range,
                            sweep.station.latitude,
                            sweep.station.longitude,
                        )
                    )


                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p2.x, p2.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))

//                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p4.x, p4.y, cmap.rescale(data)))
//                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))

                gateCount++
            }
        }

        verts.flip()


        vao = VertexArrayObject()
        vao.bind()

        vbo = GLBufferObject()
        vbo.bind()
        vbo.uploadData(verts, GL_STATIC_DRAW)
        MemoryUtil.memFree(verts)


        vao.attrib(0, 2, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

        vao.enableAttrib(0)
        vao.enableAttrib(1)

        ibo = GLBufferObject(GL_ELEMENT_ARRAY_BUFFER)
        ibo.bind()

        val indices = MemoryUtil.memAllocInt((gateCount) * 6)
        generateIndices(indices, gateCount)
        indices.flip()
        ibo.uploadData(indices, GL_STATIC_DRAW)
        MemoryUtil.memFree(indices)
        initialized = true
    }

    override fun draw(camera: Camera) {
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        cmapTexture.bind()
        vao.bind()
        vbo.bind()

        vao.attrib(0, 2, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

        vao.enableAttrib(0)
        vao.enableAttrib(1)

        ibo.bind()
        glDrawElements(GL_TRIANGLES, gateCount * 6, GL_UNSIGNED_INT, 0)
        vao.unbind()
    }

    override fun destroy() {
        vao.destroy()
        vbo.destroy()
        ibo.destroy()
    }

    private fun generateIndices(indices: IntBuffer, gateCount: Int) {
        var index = 0
        for (j in 0..<gateCount) {
            val i = index
            indices.put(i)
            indices.put(i + 1)
            indices.put(i + 2)
            indices.put(i + 2)
            indices.put(i + 3)
            indices.put(i)
            index += 4
        }
    }

    fun initialized(): Boolean {
        return initialized
    }
}