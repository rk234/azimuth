package rendering

import data.resources.ColormapManager
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import map.projection.MercatorProjection
import map.projection.aerToGeo
import meteo.radar.RadarGate
import meteo.radar.RadarSweep
import org.lwjgl.opengl.GL45.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer
import java.nio.IntBuffer

class RadarSweepRenderable(private val sweep: RadarSweep, private val radarShader: ShaderProgram, private val cmapTexture: Texture1D) : Renderable {
    private var gateCount: Int = 0
    private lateinit var vbo: GLBufferObject
    private lateinit var ibo: GLBufferObject
    private var initialized = false

    var hasGeometry = false
    private lateinit var vertBuffer: FloatBuffer
    private lateinit var indexBuffer: IntBuffer

    private val geomMutex = Mutex()
    private val graphicsMutex = Mutex()

    suspend fun createGeometry() = coroutineScope {
        geomMutex.withLock {
            if (hasGeometry || initialized) return@coroutineScope
            val jobStartIdx: IntArray = IntArray(sweep.radials.size + 1)
            for ((i, radial) in sweep.radials.withIndex()) {
                jobStartIdx[i] = gateCount
                gateCount += radial.gates.capacity()
            }
            jobStartIdx[jobStartIdx.lastIndex] = gateCount

            vertBuffer = MemoryUtil.memAllocFloat(gateCount * 3 * 4)
            val proj = MercatorProjection()
            val cmap = ColormapManager.instance.getDefault(sweep.product)

            val resolution =
                360.0f / sweep.radials.size
            val gateSize: Float = sweep.gateWidth
            val jobs = mutableListOf<Deferred<Unit>>()

            val startTime = System.currentTimeMillis()
            for ((i, radial) in sweep.radials.withIndex()) {
                jobs.add(async(Dispatchers.Default) {
                    val azimuth = radial.azimuth
                    val startAngle = (azimuth.toDouble()) - (resolution / 2) * 1.12f
                    val endAngle = (azimuth.toDouble()) + (resolution / 2) * 1.12f
                    val quad = FloatArray(12)
                    for (gateIndex in jobStartIdx[i]..<jobStartIdx[i + 1]) {
                        val gate = RadarGate(radial.gates.get(gateIndex - jobStartIdx[i]))
                        val range = sweep.rangeStart + (gateSize * gate.idx().toFloat()) // 1000
                        val data = gate.scaledValue(sweep.scale, sweep.addOffset)

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


                        val rescaled = cmap.rescale(data)
                        val gateVertIndex = gateIndex * 4 * 3
                        quad[0] = p1.x
                        quad[1] = p1.y
                        quad[2] = rescaled

                        quad[3] = p2.x
                        quad[4] = p2.y
                        quad[5] = rescaled

                        quad[6] = p3.x
                        quad[7] = p3.y
                        quad[8] = rescaled

                        quad[9] = p4.x
                        quad[10] = p4.y
                        quad[11] = rescaled

                        vertBuffer.put(gateVertIndex, quad)
                    }
                })
            }

            jobs.awaitAll()

            val dur = System.currentTimeMillis() - startTime
            println("VERT GEN: ${dur}ms")
//        vertBuffer.flip()

            val iboGenStart = System.currentTimeMillis()
            indexBuffer = MemoryUtil.memAllocInt(gateCount * 6)

            indexBuffer = MemoryUtil.memAllocInt((gateCount) * 6)
            generateIndices(indexBuffer, gateCount)
            indexBuffer.flip()
            println("IBO GEN: ${System.currentTimeMillis() - iboGenStart}ms")
            hasGeometry = true
        }
    }

    override fun init(vaoContext: VAOContext) {
        if(initialized) return
        if(!hasGeometry) {
            runBlocking {
                createGeometry()
            }
        }

        val vao = vaoContext.getVAO(this)
        vao.bind()

        vbo = GLBufferObject()
        vbo.bind()
        vbo.uploadData(vertBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(vertBuffer)

        vao.attrib(0, 2, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

        vao.enableAttrib(0)
        vao.enableAttrib(1)

        ibo = GLBufferObject(GL_ELEMENT_ARRAY_BUFFER)
        ibo.bind()

        ibo.uploadData(indexBuffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(indexBuffer)
        initialized = true
        hasGeometry = false
    }

    override fun draw(camera: Camera, vaoContext: VAOContext) {
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        cmapTexture.bind()
        val vao = vaoContext.getVAO(this) { vao ->
            vao.bind()
            vbo.bind()

            vao.attrib(0, 2, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
            vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())

            vao.enableAttrib(0)
            vao.enableAttrib(1)

            ibo.bind()
        }
        vao.bind()
        glDrawElements(GL_TRIANGLES, gateCount * 6, GL_UNSIGNED_INT, 0)
    }

    override fun destroy() {
        //TODO pass vao context here
//        vao.destroy()
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