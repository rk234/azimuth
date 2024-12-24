package meteo.radar

import com.formdev.flatlaf.extras.FlatSVGIcon
import org.joml.Vector3f
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

class Colormap(private val name: String, private val str: String) {
    var unit: String? = null

    val steps: ArrayList<ColorStep> = arrayListOf()

    init {
        parse(str)
    }

    private fun parse(colormapStr: String) {
        val lines = colormapStr.split("\n").map { line -> line.trim().lowercase() }
        val colorLines = arrayListOf<String>()

        for (line in lines) {
            val (prop, value) = line.split(":").map { s -> s.trim() }

            when (prop) {
                "product" -> println("Loading colormap for: $value")
                "units" -> unit = value
                "color" -> {
                    colorLines.add(value)
                }
            }
        }

        for (i in 0..<colorLines.size - 1) {
            val s1 = parseColormapLine(colorLines[i])
            val s2 = parseColormapLine(colorLines[i + 1])

            steps.add(ColorStep(s1.first, s2.first, s1.second, s2.second))
        }
    }

    private fun parseColormapLine(str: String): Triple<Float, Vector3f, Vector3f?> {
        val fields = str.split(" ").filter { s -> s.trim().isNotEmpty() }.map { s -> s.toFloat() }
        val (dataVal, r, g, b) = fields;
        if (fields.size > 4) {
            val (r1, g1, b1) = fields.slice(4..<fields.size)
            return Triple(dataVal, Vector3f(r, g, b).mul(1 / 255f), Vector3f(r1, g1, b1))
        } else {
            return Triple(dataVal, Vector3f(r, g, b).mul(1 / 255f), null)
        }
    }

    fun sample(value: Float): Vector3f {
        var min = 0;
        var max = steps.size - 1

        if (value <= steps.first().low) return Vector3f(steps.first().lowColor)
        if (value >= steps.last().high) return Vector3f(steps.last().highColor)

        do {
            val step = steps[(min + max) / 2]
            if (value < step.low) {
                max = -1 + (min + max) / 2
            }
            if (value > step.high) {
                min = 1 + (min + max) / 2
            }
        } while (!steps[(min + max) / 2].contains(value))

        return steps[(min + max) / 2].sample(value)
    }

    fun genTextureData(samples: Int, out: ByteBuffer) {
        val min = steps.first().low
        val max = steps.last().high
        val step = (max - min) / samples

        for (i in 0..<samples) {
            val color = sample(min + (step * i)).mul(255f)
            out.put(color.x.toInt().toByte())
            out.put(color.y.toInt().toByte())
            out.put(color.z.toInt().toByte())
        }
    }

    fun rescale(data: Number): Float {
        val min = steps.first().low
        val max = steps.last().high
        return Math.clamp((data.toFloat() - min) / (max - min), 0f, 1f)
    }

    data class ColorStep(val low: Float, val high: Float, val lowColor: Vector3f, val highColor: Vector3f) {
        fun contains(value: Float) = value in low..high

        fun sample(value: Float): Vector3f {
            if (value <= low) {
                return Vector3f(lowColor)
            } else if (value >= high) {
                return Vector3f(highColor)
            } else {
                val out = Vector3f(0f)
                val t = (value - low) / (high - low)
                lowColor.lerp(highColor, t, out)
                return out
            }
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return if(other is Colormap) {
            this.name == other.name
        } else {
            Objects.equals(this, other)
        }
    }
}
