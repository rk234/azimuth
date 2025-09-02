package map.layers

import data.resources.ShaderManager
import data.state.AppState
import data.warnings.Warning
import data.warnings.WarningDataManager
import data.warnings.WarningType
import kotlinx.coroutines.runBlocking
import map.projection.MercatorProjection
import org.joml.Vector2f
import org.joml.Vector3f
import rendering.Camera
import rendering.PathRenderable
import rendering.ShaderProgram
import rendering.VAOContext
import utils.invokeLaterOnRenderThread
import utils.tripleToVec3f

class WarningLayer(private val warningDataManager: WarningDataManager, private val warningType: WarningType) : MapLayer {
    private lateinit var shader: ShaderProgram
    private var paths: ArrayList<PathRenderable> = ArrayList()
    private val vertsPerChunk = 60_000
    private var initialized = false

    private fun initGraphics() {
        shader = ShaderManager.instance.linesShader()
    }

    init {
        AppState.warningDataManager.addListener { warnings ->
            runBlocking {
                invokeLaterOnRenderThread {
                    updateWarnings(warnings)
                }
            }
        }
    }

    private fun updateWarnings(newWarnings: List<Warning>) {
        if(initialized) destroy()
        createGeometry(newWarnings)
        initialized = true
    }

    override fun init(camera: Camera, vaoContext: VAOContext) {
        if(initialized) return
        initGraphics()
        createGeometry(warningDataManager.getWarnings())
        initialized = true
    }

    private fun createGeometry(warnings: List<Warning>) {
        val vertices = arrayListOf<Vector2f>()
        val proj = MercatorProjection()

        warnings
            .filter { it.type == warningType }
            .forEach { warning ->
                println("Generating geometry for warning: ${warning.type}, polygons: ${warning.polygons.size}")
                warning.polygons.forEach { poly ->
                    //add first line twice
                    vertices.add(
                        proj.toCartesian(Vector2f(poly.coordinates[0].lat, poly.coordinates[0].lon)),
                    )
                    vertices.add(
                        proj.toCartesian(Vector2f(poly.coordinates[1].lat, poly.coordinates[1].lon)),
                    )

                    poly.coordinates.windowed(2, 1).map { coords ->
                        vertices.add(
                            proj.toCartesian(Vector2f(coords[0].lat, coords[0].lon)),
                        )
                        vertices.add(
                            proj.toCartesian(Vector2f(coords[1].lat, coords[1].lon)),
                        )
                    }

                    if (poly.coordinates.isNotEmpty() && (poly.coordinates.first() != poly.coordinates.last())) {
                        val first = poly.coordinates.first()
                        val last = poly.coordinates.last()
                        vertices.add(
                            proj.toCartesian(
                                Vector2f(last.lat, last.lon)
                            )
                        )
                        vertices.add(
                            proj.toCartesian(
                                Vector2f(first.lat, first.lon)
                            )
                        )
                    }
                }
            }

        val chunks = vertices.chunked(vertsPerChunk)
        chunks.forEach { c ->
            paths.add(PathRenderable(c, shader, 0.08f, tripleToVec3f(WarningType.color(warningType)), -10f, 0.05f, Vector3f(0f, 0f, 0f)))
        }
    }

    override fun render(camera: Camera, vaoContext: VAOContext) {
        if(!initialized) return
        for(path in paths) {
            if(!path.initialized()) path.init(vaoContext)

            path.draw(camera, vaoContext)
        }
    }

    override fun destroy() {
        for(path in paths) {
            path.destroy()
        }
        paths.clear()
        initialized = false
    }

    override fun initialized(): Boolean {
        return initialized
    }
}