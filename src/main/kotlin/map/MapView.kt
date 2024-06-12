package map

import RadarVolume
import map.layers.GeoJSONLayer
import map.projection.MercatorProjection
import map.projection.aerToGeo
import meteo.radar.Product
import org.joml.Vector2f
import org.joml.Vector3f
import org.json.JSONObject
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL45.*;
import org.lwjgl.opengl.awt.AWTGLCanvas
import org.lwjgl.opengl.awt.GLData
import org.lwjgl.system.MemoryUtil
import rendering.*
import ucar.nc2.NetcdfFiles
import java.awt.Cursor

import java.io.File
import kotlin.math.*

class MapView(data: GLData?) : AWTGLCanvas(data) {
    lateinit var vertexBuffer: GLBufferObject;
    lateinit var vertexArrayObject: VertexArrayObject;
    lateinit var shader: ShaderProgram;
    lateinit var camera: Camera
    lateinit var inputHandler: MapViewInputHandler
    lateinit var cmapTexture: Texture1D
    var numVerts = 0;

    lateinit var renderer: Renderer

    init {
        cursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
    }

    override fun initGL() {
        println("GL Version: ${effective.majorVersion}.${effective.minorVersion}")
        GL.createCapabilities()
        glClearColor(0f, 0.0f, 0.0f, 1.0f)

        val geojson = JSONObject(File("src/main/resources/geo/countries.geojson").readText(Charsets.UTF_8))
        val geojsonLayer = GeoJSONLayer(geojson)

        camera = Camera(width.toFloat(), height.toFloat())
        renderer = Renderer(camera)
        inputHandler = MapViewInputHandler(camera)

        geojsonLayer.init(camera)

        addMouseMotionListener(inputHandler)
        addMouseWheelListener(inputHandler)
        addMouseListener(inputHandler)

        val vsSource = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        shader = ShaderProgram()
        shader.createVertexShader(vsSource)
        shader.createFragmentShader(fsSource)
        shader.link()

        val file = NetcdfFiles.openInMemory("src/main/resources/KLWX_20240119_153921");
        val vol = RadarVolume(file, Product.REFLECTIVITY_HIRES)
        val proj = MercatorProjection()
        println(file.variables)
        println(vol.station)
        println(vol.latitude)
        println(vol.longitude)
        println(vol.elevationMeters)
        val camPos = proj.toCartesian(Vector2f(vol.latitude, vol.longitude))
        println("CAMERA POSITION: $camPos")
        camera.position = Vector3f(camPos.x, camPos.y, 0f)
        camera.zoom = 0.001f
        camera.recalcProjection()
        camera.recalcTransform()

        val cmap = vol.product.colormap

        val colormapImageData = MemoryUtil.memAlloc(100 * 3)
        cmap.genTextureData(100, colormapImageData)
        println("Image Data: ${colormapImageData.get(0)}")
        cmapTexture = Texture1D()
        cmapTexture.bind()
        cmapTexture.uploadData(100, colormapImageData.flip())
        MemoryUtil.memFree(colormapImageData)

        val verts = MemoryUtil.memAllocFloat(720 * 1832 * 3 * 6)

        val firstScan = vol.scans[0]

        val resolution = 360.0f / firstScan.radials.size //TODO: Should be 1 degree for normal resolution, .5 for super-res
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
                val latlng =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            firstScan.elevation,
                            range,
                            vol.latitude,
                            vol.longitude
                        )
                    )
                println("LAT LON: $latlng")

                val p1 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            firstScan.elevation,
                            range,
                            vol.latitude,
                            vol.longitude,
                        )
                    )

                val p2 =
                    proj.toCartesian(
                        aerToGeo(
                            startAngle.toFloat(),
                            firstScan.elevation,
                            range + gateSize,
                            vol.latitude,
                            vol.longitude,
                        )
                    )
                val p3 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            firstScan.elevation,
                            range + gateSize,
                            vol.latitude,
                            vol.longitude,
                        )
                    )
                val p4 =
                    proj.toCartesian(
                        aerToGeo(
                            endAngle.toFloat(),
                            firstScan.elevation,
                            range,
                            vol.latitude,
                            vol.longitude,
                        )
                    )

//                val p1 = Vector2f(
//                    (cos(startAngle) * range).toFloat(),
//                    (-sin(startAngle) * range).toFloat()
//                )
//                val p2 = Vector2f(
//                    (cos(startAngle) * (range + gateSize)).toFloat(),
//                    (-sin(startAngle) * (range + gateSize)).toFloat()
//                )
//                val p3 = Vector2f(
//                    (cos(endAngle) * (range + gateSize)).toFloat(),
//                    (-sin(endAngle) * (range + gateSize)).toFloat()
//                )
//                val p4 = Vector2f(
//                    (cos(endAngle) * range).toFloat(),
//                    (-sin(endAngle) * range).toFloat()
//                )

                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p2.x, p2.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))

                verts.put(floatArrayOf(p3.x, p3.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p4.x, p4.y, cmap.rescale(data)))
                verts.put(floatArrayOf(p1.x, p1.y, cmap.rescale(data)))

//                verts.put(floatArrayOf(p1.x, p1.y, 0f, color.x, color.y, color.z))
//                verts.put(floatArrayOf(p2.x, p2.y, 0f, color.x, color.y, color.z))
//                verts.put(floatArrayOf(p3.x, p3.y, 0f, color.x, color.y, color.z))
//
//                verts.put(floatArrayOf(p3.x, p3.y, 0f, color.x, color.y, color.z))
//                verts.put(floatArrayOf(p4.x, p4.y, 0f, color.x, color.y, color.z))
//                verts.put(floatArrayOf(p1.x, p1.y, 0f, color.x, color.y, color.z))
                numVerts += 6
            }
        }

//        val verts = floatArrayOf(
//            0.0f, 0.5f, 1.0f,
//            -0.5f, -0.5f, 1.0f,
//            0.5f, -0.5f, 1.0f
//        )
        verts.flip()

        vertexArrayObject = VertexArrayObject()
        vertexArrayObject.bind()

        vertexBuffer = GLBufferObject()
        vertexBuffer.bind()
        vertexBuffer.uploadData(verts, GL_STATIC_DRAW)

        MemoryUtil.memFree(verts)

//        vertexArrayObject.attrib(0, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, 0)
//        vertexArrayObject.attrib(1, 3, GL_FLOAT, false, 6 * Float.SIZE_BYTES, (3 * Float.SIZE_BYTES).toLong())
        vertexArrayObject.attrib(0, 2, GL_FLOAT, false, 3 * Float.SIZE_BYTES, 0)
        vertexArrayObject.attrib(1, 1, GL_FLOAT, false, 3 * Float.SIZE_BYTES, (2 * Float.SIZE_BYTES).toLong())
    }

    override fun paintGL() {
        glViewport(0, 0, width, height)
        shader.bind()

        camera.updateViewport(width.toFloat() / 100, height.toFloat() / 100)
        shader.setUniformMatrix4f("projectionMatrix", camera.projectionMatrix)
        shader.setUniformMatrix4f("transformMatrix", camera.transformMatrix)

        vertexBuffer.bind()
        cmapTexture.bind()
        vertexArrayObject.bind()
        vertexArrayObject.enableAttrib(0)
        vertexArrayObject.enableAttrib(1)

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glDrawArrays(GL_TRIANGLES, 0, numVerts)
        swapBuffers()
    }
}