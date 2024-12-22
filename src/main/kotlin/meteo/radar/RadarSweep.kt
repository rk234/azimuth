package meteo.radar

data class RadarSweep(var elevation: Float, var radials: ArrayList<List<RadarGate>>, var station: Station, var product: Product, var numRadials: Int, var numGates: Int)