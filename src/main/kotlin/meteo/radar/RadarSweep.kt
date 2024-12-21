package meteo.radar

data class RadarSweep(val elevation: Float, val radials: List<List<RadarGate>>, val station: Station, val product: Product, val numRadials: Int, val numGates: Int)