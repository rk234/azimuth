package meteo.radar

import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class RadarRenderData {
    //TODO: Geodesic forward calculation to convert radar station latitude/longitude and azimuth/range to new latitude/longitude point
    /*
    https://github.com/ARM-DOE/pyart/issues/247
    def xy_to_latlon(radar):
        rng, az = np.meshgrid(radar.range['data'], radar.azimuth['data'])
        rng, ele = np.meshgrid(radar.range['data'], radar.elevation['data'])
        theta_e = ele * np.pi / 180.0       # elevation angle in radians.
        theta_a = az * np.pi / 180.0        # azimuth angle in radians.
        Re = 6371.0 * 1000.0 * 4.0 / 3.0     # effective radius of earth in meters.
        r = rng * 1000.0                    # distances to gates in meters.

        z = (r ** 2 + Re ** 2 + 2.0 * r * Re * np.sin(theta_e)) ** 0.5 - Re
        s = Re * np.arcsin(r * np.cos(theta_e) / (Re + z))  # arc length in m.
        x = s * np.sin(theta_a)
        y = s * np.cos(theta_a)

        c = np.sqrt(x*x + y*y) / r
        phi_0 = radar.latitude['data'] * np.pi / 180
        azi = np.arctan2(y, x)  # from east to north

        lat = np.arcsin(np.cos(c) * np.sin(phi_0) +
                        np.sin(azi) * np.sin(c) * np.cos(phi_0)) * 180 / np.pi
        lon = (np.arctan2(np.cos(azi) * np.sin(c), np.cos(c) * np.cos(phi_0) -
               np.sin(azi) * np.sin(c) * np.sin(phi_0)) * 180 /
                np.pi + radar.longitude['data'])
        lon = np.fmod(lon + 180, 360) - 180

        lat_axis = {
            'data':  lat,
            'long_name': 'Latitude for points in Cartesian system',
            'axis': 'YX',
            'units': 'degree_N',
            'standard_name': 'latitude',
        }

        lon_axis = {
            'data': lon,
            'long_name': 'Longitude for points in Cartesian system',
            'axis': 'YX',
            'units': 'degree_E',
            'standard_name': 'longitude',
        }
        return lat_axis, lon_axis
     */
    fun azimuthRangeElevationToLatLon(
        stationLatitude: Float,
        stationLongitude: Float,
        stationHeightMeters: Float,
        azimuth: Float,
        range: Float,
        gateElevationMeters: Float
    ): Pair<Float, Float> {
    }
}