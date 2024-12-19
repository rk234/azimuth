package data

import rendering.ShaderProgram
import java.io.File

class ShaderManager {
    companion object {
        lateinit var instance: ShaderManager

        fun init() {
           instance = ShaderManager()
        }
    }

    private var shaders = mutableMapOf<String, ShaderProgram>()

    fun radarShader(): ShaderProgram {
        if(shaders.containsKey("radar")) return shaders["radar"]!!
        else return loadRadarShader()
    }

    fun linesShader(): ShaderProgram {
        if(shaders.containsKey("lines")) return shaders["lines"]!!
        else return loadLinesShader()
    }

    private fun loadRadarShader() : ShaderProgram {
        val vsSource = File("src/main/resources/shaders/radar/radar.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/radar/radar.fs.glsl").readText(Charsets.UTF_8)

        val radarShader = ShaderProgram()
        radarShader.createVertexShader(vsSource)
        radarShader.createFragmentShader(fsSource)
        radarShader.link()
        shaders["radar"] = radarShader
        return radarShader
    }

    private fun loadLinesShader() : ShaderProgram {
        val vsSource = File("src/main/resources/shaders/lines/lines.vs.glsl").readText(Charsets.UTF_8)
        val fsSource = File("src/main/resources/shaders/lines/lines.fs.glsl").readText(Charsets.UTF_8)

        val linesShader = ShaderProgram()
        linesShader.createVertexShader(vsSource)
        linesShader.createFragmentShader(fsSource)
        linesShader.link()
        shaders["lines"] = linesShader
        return linesShader
    }
}