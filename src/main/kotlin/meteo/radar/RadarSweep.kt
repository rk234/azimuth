package meteo.radar

data class RadarSweep(var elevation: Float, var radials: ArrayList<ArrayList<RadarGate>>, var station: Station, var product: Product, var numRadials: Int, var numGates: Int)