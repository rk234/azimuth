package map.geometry

data class GeoLatLon(
    val lat: Float,
    val lon: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GeoLatLon) return false

        if (lat != other.lat) return false
        if (lon != other.lon) return false

        return true
    }
    override fun toString(): String {
        return "GeoLatLon(lat=$lat, lon=$lon)"
    }
}
