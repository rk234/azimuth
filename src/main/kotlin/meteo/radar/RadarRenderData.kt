package meteo.radar

import kotlin.math.*

class RadarRenderData {

    fun azimuthRangeElevationToLatLon(
        stationLatitude: Float,
        stationLongitude: Float,
        azimuth: Float,
        range: Float,
        elevationAngle: Float
    ): Pair<Float, Float> {
        val theta_e = Math.toRadians(elevationAngle.toDouble())
        val theta_a = Math.toRadians(azimuth.toDouble())
        val Re = 6371.0 * 1000.0 * 4.0 / 3.0 // Earth radius in meters
        val r = range // already in meters

        val z = (r.pow(2) + Re.pow(2) + 2 * r * Re * sin(theta_e)).pow(0.5) - Re
        val s = Re * asin(r * cos(theta_e) / (Re + z))
        val x = s * sin(theta_a)
        val y = s * cos(theta_a)

        val c = sqrt(x*x + y*y) / r
        val phi_0 = Math.toRadians(stationLatitude.toDouble())
        val azi = atan2(y, x)

        val lat = asin(cos(c) * sin(phi_0) + sin(azi) * sin(c) * cos(phi_0)) * 180 / PI
        val lon = (atan2(cos(azi) * sin(c), cos(c) * cos(phi_0) - sin(azi) * sin(c) * sin(phi_0)) * 180 / PI + stationLongitude)

        return Pair(lat.toFloat(), (((lon + 180) % 360) - 180).toFloat())
    }
}