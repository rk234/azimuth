package meteo.radar

import java.io.File

enum class Product(
    val displayName: String,
    val dataField: String,
    val elevationField: String,
    val azimuthField: String,
    val distanceField: String,
    val colormap: Colormap
) {
    REFLECTIVITY_HIRES(
        "Base Reflectivity (Hi-Res)",
        "Reflectivity_HI",
        "elevationR_HI",
        "azimuthR_HI",
        "distanceR_HI",
        Colormap(File("src/main/resources/colormaps/reflectivity.cmap").readText())
    ),
    RADIAL_VEL_HIRES(
        "Radial Velocity (Hi-Res)", "RadialVelocity_HI", "elevationV_HI", "azimuthV_HI", "distanceV_HI",
        Colormap(File("src/main/resources/colormaps/velocity.cmap").readText())
    ),
    CORRELATION_COEF_HIRES(
        "Correlation Coefficient (Hi-Res)",
        "CorrelationCoefficient_HI", "elevationC_HI", "azimuthC_HI", "distanceC_HI",
        Colormap(File("src/main/resources/colormaps/crosscorrelation.cmap").readText())
    ),
    REFLECTIVITY(
        "Base Reflectivity",
        "Reflectivity",
        "elevationR",
        "azimuthR",
        "distanceR",
        Colormap(File("src/main/resources/colormaps/reflectivity.cmap").readText())
    )
}