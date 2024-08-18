package map.layers

import RadarVolume
import map.projection.MercatorProjection
import map.projection.aerToGeo
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File

class RadarLayer(private val volume: RadarVolume, private val tilt: Int) : MapLayer {
    private lateinit var vbo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var radarShader: ShaderProgram
    private lateinit var cmapTexture: Texture1D
    private var numVerts: Int = 0

    override fun init(camera: Camera) {
        val vsSource = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        radarShader = ShaderProgram()
        radarShader.createVertexShader(vsSource)
        radarShader.createFragmentShader(fsSource)
        radarShader.link()


        val verts = MemoryUtil.memAllocFloat(720 * 1832 * 3 * 6)
        val firstScan = volume.scans[tilt]
        val proj = MercatorProjection()
        val cmap = volume.product.colormap

        val colormapImageData = MemoryUtil.memAlloc(100 * 3)
        cmap.genTextureData(100, colormapImageData)
        println("Image Data: ${colormapImageData.get(0)}")
        cmapTexture = Texture1D()
        cmapTexture.bind()
        cmapTexture.uploadData(100, colormapImageData.flip())
        MemoryUtil.memFree(colormapImageData)

        val resolution =
            360.0f / firstScan.radials.size //TODO: Should be 1 degree for normal resolution, .5 for super-res
        var gateSize: Float = -1f
        for ((radialIndex, radial) in firstScan.radials.withIndex()) {
            for ((gateIndex, gate) in radial.withIndex()) {
                val azimuth = gate.azimuthDeg
                val range = gate.rangeMeters // 1000
                val data = gate.data

                val startAngle = (azimuth.toDouble()) - (resolution / 2) * 1.05f
                val endAngle = (azimuth.toDouble()) + (resolution / 2) * 1.05f

                if (gateSize == -1f) {
                    gateSize = (radial[gateIndex + 1].rangeMeters - radial[gateIndex].rangeMeters)
                }

                val p1 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            firstScan.elevation,
                            range,
                            volume.latitude,
                            volume.longitude,
                        )
                    )

                val p2 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            firstScan.elevation,
                            range + gateSize,
                            volume.latitude,
                            volume.longitude,
                        )
                    )
                val p3 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            firstScan.elevation,
                            range + gateSize,
                            volume.latitude,
                            volume.longitude,
                        )
                    )
                val p4 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            firstScan.elevation,
                            range,
                            volume.latitude,
                            volume.longitude,
                        )
                    )


                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p2.x, p2.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))

                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p4.x, p4.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))

                numVerts += 6
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
//        vao.attrib(2, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
//        vao.enableAttrib(0)
//        vao.enableAttrib(1)

        println("verts: $numVerts")
        println(volume.station)
        println(volume.latitude)
        println(volume.longitude)
        println(volume.elevationMeters)

        val camPos = proj.toCartesian(Vector2f(volume.latitude, volume.longitude))
        camera.position = Vector3f(camPos.x, camPos.y, 0f)
        camera.zoom = 0.001f
        camera.recalcProjection()
        camera.recalcTransform()
    }

    override fun render(camera: Camera) {
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vbo.bind()
        cmapTexture.bind()
        vao.bind()
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        glDrawArrays(GL_TRIANGLES, 0, numVerts)
    }

    override fun destroy() {
        vbo.destroy()
        vao.destroy()
        radarShader.destroy()
    }
}