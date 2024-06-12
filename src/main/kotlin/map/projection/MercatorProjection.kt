package map.projection

import org.joml.Vector2f
import kotlin.math.*

class MercatorProjection : MapProjection {
    val radiusMajor = 6378137.0
    val radiusMinor = 6356752.3142


    //https://en.wikipedia.org/wiki/Mercator_projection
    override fun toCartesian(latlon: Vector2f): Vector2f {
        val pixelsPerLonDegree = (700 * 250) / 360
        val pixelsPerLonRadian = (700 * 250) / (2 * PI)
        val x = latlon.y * pixelsPerLonDegree

        val siny = Math.clamp(sin(Math.toRadians(latlon.x.toDouble())), -0.999999, 0.999999)
        val y = 0.5 * Math.log((1 + siny) / (1 - siny)) * -pixelsPerLonRadian

        return Vector2f(x, y.toFloat())
    }

    override fun fromCartesian(point: Vector2f): Vector2f {
        TODO("Not yet implemented")
    }
}