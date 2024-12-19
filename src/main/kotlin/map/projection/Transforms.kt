package map.projection

import org.joml.Vector2f
import org.joml.Vector3f
import kotlin.math.*

//https://arm-doe.github.io/pyart/_modules/pyart/core/transforms.html

fun aerToGeo(azimuth: Float, elevation: Float, range: Float, stationLat: Float, stationLon: Float): Vector2f {
    val cartesian = antennaToCartesian(azimuth, range, elevation)
    val latLon = antennaRelativeCartesianToGeographic(cartesian.x, cartesian.y, stationLat, stationLon)
    return latLon
}

fun antennaToCartesian(azimuth: Float, range: Float, elevation: Float): Vector3f {
    val thetaE = Math.toRadians(elevation.toDouble())
    val thetaA = Math.toRadians(azimuth.toDouble())
    val R = 6371.0 * 1000.0 * 4.0 / 3.0
    val r = range

    val z = (r.pow(2) + R.pow(2) + 2 * r * R * sin(thetaE)).pow(0.5) - R
    val s = R * asin(r * cos(thetaE) / (R + z))
    val x = s * sin(thetaA)
    val y = s * cos(thetaA)

    return Vector3f(x.toFloat(), y.toFloat(), z.toFloat())
}

fun antennaRelativeCartesianToGeographic(x: Float, y: Float, antennaLat: Float, antennaLon: Float): Vector2f {
    val R = 6370997.0
    val antennaLatRad = Math.toRadians(antennaLat.toDouble())
    val antennaLonRad = Math.toRadians(antennaLon.toDouble())

    val rho = sqrt(x * x + y * y)
    val c = rho / R

    var latDeg: Double = 0.0
    if (rho != 0.0f) {
        val latRad = asin(
            cos(c) * sin(antennaLatRad) + y * sin(c) * cos(antennaLatRad) / rho
        )
        latDeg = Math.toDegrees(latRad)
    } else {
        latDeg = antennaLat.toDouble()
    }

    val x1 = x * sin(c)
    val x2 = rho * cos(antennaLatRad) * cos(c) - y * sin(antennaLatRad) * sin(c)

    val lonRad = antennaLonRad + atan2(x1, x2)
    var lonDeg = Math.toDegrees(lonRad)

    if (lonDeg > 180) lonDeg -= 360.0
    if (lonDeg < -180) lonDeg += 360.0

    return Vector2f(latDeg.toFloat(), lonDeg.toFloat())
}