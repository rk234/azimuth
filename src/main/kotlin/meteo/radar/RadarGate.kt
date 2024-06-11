package meteo.radar

data class RadarGate<T>(val elevationDeg: Float, val azimuthDeg: Float, val rangeMeters: Float, val data: T)