package meteo.radar

data class RadarScan(val elevation: Float, val radials: List<List<RadarGate>>, val station: Station, val product: Product)