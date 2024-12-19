package meteo.radar

data class Station(val code: String, val name: String, val latitude: Float, val longitude: Float, val elevation: Float, val vcp: Int, val vcpName: String, val title: String)
