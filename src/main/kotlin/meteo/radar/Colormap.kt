package meteo.radar

import org.joml.Vector3f

class Colormap(str: String) {
    var product: Product? = null
    var unit: String? = null

    lateinit var min: Float
    lateinit var max: Float

    val steps: ArrayList<ColorStep> = arrayListOf()

    init {
        parse(str)
    }

    private fun parse(colormapStr: String) {
        val lines = colormapStr.split("\n").map { line -> line.trim() }
        val colorLines = arrayListOf<String>()

        for(line in lines) {
            val (prop, value) = line.split(":").map {s -> s.trim()}

            when(prop) {
                "product" -> product = Product.fromString(value)
                "units" -> unit = value
                "color" -> {
                   colorLines.add(value)
                }
            }
        }

        for(i in 0..<colorLines.size-1) {
            val fields = colorLines[i].split(" ").map { s -> s.toFloat() }
            val (dataVal, r, g, b) = fields;
            var (r1, g1, b1) = fields.slice(4..<fields.size)

        }
    }

    private fun parseColorStep(str: String): ColorStep {
        val fields = str.split(" ").map { s -> s.toFloat() }
        val (dataVal, r, g, b) = fields;
        var (r1, g1, b1) = fields.slice(4..<fields.size)
    }
}

data class ColorStep(val low: Float, val high: Float, val lowColor: Vector3f, val highColor: Vector3f) {
    fun sample(value: Float): Vector3f {
        if(value <= low) {
            return lowColor
        } else if(value >= high) {
            return highColor
        } else {
            val out = Vector3f(0f)
            val t = (value-low)/(high - low)
            lowColor.lerp(highColor, t, out)
            return out
        }
    }
}