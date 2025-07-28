package map.geometry

data class GeoPolygon(
    val coordinates: List<GeoLatLon>
) {
    override fun toString(): String {
        return "GeoPolygon(coordinates=$coordinates)"
    }
}
