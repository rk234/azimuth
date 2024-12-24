package meteo.radar

data class RadarSweep(val fileName: String, val tiltIndex: Int, val elevation: Float, val radials: List<RadarRadial>, val station: Station, val product: Product, val numRadials: Int, val numGates: Int, val rangeStart: Float, val gateWidth: Float, val scale: Float, val addOffset: Float)