package meteo.radar

enum class Product(
    val dataField: String,
    val elevationField: String,
    val azimuthField: String,
    val distanceField: String
) {
    REFLECTIVITY_HIRES("Reflectivity_HI", "elevationR_HI", "azimuthR_HI", "distanceR_HI"),
    RADIAL_VEL_HIRES("RadialVelocity_HI", "elevationV_HI", "azimuthV_HI", "distanceV_HI"),
    CORRELATION_COEF_HIRES("CorrelationCoefficient_HI", "elevationC_HI", "azimuthC_HI", "distanceC_HI");

    companion object {
        fun fromString(str: String): Product? {
            when (str) {
                "REF" -> return Product.REFLECTIVITY_HIRES
                "VEL" -> return Product.RADIAL_VEL_HIRES
                "RHO" -> return Product.CORRELATION_COEF_HIRES
            }
            return null
        }
    }
}