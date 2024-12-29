package map.projection

import org.joml.Vector2f
import kotlin.math.*

class MercatorProjection : MapProjection {
    val radiusMajor = 6378137.0
    val radiusMinor = 6356752.3142


    //https://en.wikipedia.org/wiki/Mercator_projection
    override inline fun toCartesian(latlon: Vector2f): Vector2f {
        val x = Math.toRadians(latlon.y.toDouble()) * radiusMajor
        val y = ln(tan(PI / 4 + Math.toRadians(latlon.x.toDouble()) / 2)) * radiusMajor

        return Vector2f(x.toFloat(), y.toFloat())
    }

    override inline fun fromCartesian(point: Vector2f): Vector2f {
        val lon = Math.toDegrees(point.x.toDouble() / radiusMajor)
        val lat = Math.toDegrees(2 * atan(exp(point.y.toDouble() / radiusMajor)) - PI / 2)
        return Vector2f(lat.toFloat(), lon.toFloat())
    }
}