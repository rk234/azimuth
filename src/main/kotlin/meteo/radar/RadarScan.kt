package meteo.radar

data class RadarScan<T>(val elevation: Float, val radials: List<List<RadarGate<T>>>)