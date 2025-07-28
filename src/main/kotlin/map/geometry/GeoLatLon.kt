package map.geometry

data class GeoLatLon(
    val lat: Float,
    val lon: Float
) {
    override fun toString(): String {
        return "GeoLatLon(lat=$lat, lon=$lon)"
    }
}
