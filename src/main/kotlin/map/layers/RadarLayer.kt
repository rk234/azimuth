package map.layers

import RadarVolume
import map.projection.MercatorProjection
import map.projection.aerToGeo
import meteo.radar.Colormap
import org.lwjgl.opengl.GL45.*;
import meteo.radar.Product
import org.lwjgl.system.MemoryUtil
import rendering.*
import java.io.File

class RadarLayer(val volume: RadarVolume, val tilt: Int) : MapLayer {
    private lateinit var vbo: GLBufferObject
    private lateinit var vao: VertexArrayObject
    private lateinit var radarShader: ShaderProgram
    private lateinit var colormapTexture: Texture1D
    private lateinit var camera: Camera
    private var numVerts: Int = 0

    override fun init(camera: Camera) {
        val vsSource = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        radarShader = ShaderProgram()
        radarShader.createVertexShader(vsSource)
        radarShader.createFragmentShader(fsSource)
        radarShader.link()

        vao = VertexArrayObject()
        vao.bind()

        val verts = MemoryUtil.memAllocFloat(720 * 1832 * 3 * 6)
        val firstScan = volume.scans[0]
        val proj = MercatorProjection()
        val cmap = volume.product.colormap

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

        vbo = GLBufferObject()
        vbo.bind()

        verts.flip()
        vbo.uploadData(verts, GL_STATIC_DRAW)
        MemoryUtil.memFree(verts)

        vao.attrib(0, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vao.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (1 * Float.SIZE_BYTES).toLong())
        vao.attrib(2, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        vao.enableAttrib(2)
    }

    override fun render(renderer: Renderer) {
        radarShader.bind()
        radarShader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        radarShader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vbo.bind()
        colormapTexture.bind()
        vao.bind()
        vao.enableAttrib(0)
        vao.enableAttrib(1)
        vao.enableAttrib(2)
        glDrawArrays(GL_TRIANGLES, 0, numVerts)
    }

    override fun destroy() {
        vbo.destroy()
        vao.destroy()
        radarShader.destroy()
    }
}