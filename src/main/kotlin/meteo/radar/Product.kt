package meteo.radar

import java.io.File

enum class Product(
    val displayName: String,
    val dataField: String,
    val elevationField: String,
    val azimuthField: String,
    val distanceField: String,
) {
    REFLECTIVITY_HIRES(
        "Base Reflectivity (Hi-Res)",
        "Reflectivity_HI",
        "elevationR_HI",
        "azimuthR_HI",
        "distanceR_HI",
    ),
    RADIAL_VEL_HIRES(
        "Radial Velocity (Hi-Res)", "RadialVelocity_HI", "elevationV_HI", "azimuthV_HI", "distanceV_HI",
    ),
    CORRELATION_COEF_HIRES(
        "Correlation Coefficient (Hi-Res)",
        "CorrelationCoefficient_HI", "elevationC_HI", "azimuthC_HI", "distanceC_HI",
    ),
    REFLECTIVITY(
        "Base Reflectivity",
        "Reflectivity",
        "elevationR",
        "azimuthR",
        "distanceR",
    );

    override fun toString(): String {
        return this.displayName
    }
}