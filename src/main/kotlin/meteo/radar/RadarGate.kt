package meteo.radar

data class RadarGate<T>(val elevation: Float, val azimuth: Float, val range: Float, val data: T)