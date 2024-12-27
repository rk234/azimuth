package meteo.radar

import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class RadarVolume(file: NetcdfFile, val handle: VolumeFileHandle) {
    private class ProductMetadata(file: NetcdfFile, product: Product) {
        val azimuthVar = file.findVariable(product.azimuthField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val elevationVar = file.findVariable(product.elevationField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val rangeVar = file.findVariable(product.distanceField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val variableVar = file.findVariable(product.dataField) ?: throw Exception("Unable to read product meta data from file. Product: $product")

        val addOffset = variableVar.attributes().findAttributeDouble("add_offset", 0.0).toFloat()
        val scale = variableVar.attributes().findAttributeDouble("scale_factor", 1.0).toFloat()

        val belowThreshold: Float = variableVar.attributes().findAttribute("signal_below_threshold")?.numericValue?.toFloat() ?: 0.0f
        val noData: Float = variableVar.attributes().findAttribute("missing_value")?.numericValue?.toFloat() ?: 0.0f

        val productData: ArrayByte.D3 = variableVar.read() as ArrayByte.D3
        val azimuthData: ArrayFloat.D2 = azimuthVar.read() as ArrayFloat.D2
        val elevationData: ArrayFloat.D2 = elevationVar.read() as ArrayFloat.D2
        val rangeData: ArrayFloat.D1 = rangeVar.read() as ArrayFloat.D1

        val numSweeps = productData.shape[0]
        val numRadials = productData.shape[1]
        val numGates = productData.shape[2]
    }

    val station = Station(
        file.findGlobalAttribute("Station")?.stringValue ?: "UNKNOWN",
        file.findGlobalAttribute("StationName")?.stringValue ?: "UNKNOWN",
        file.findGlobalAttribute("StationLatitude")?.numericValue?.toFloat() ?: 0f,
        file.findGlobalAttribute("StationLongitude")?.numericValue?.toFloat() ?: 0f,
        file.findGlobalAttribute("StationElevationInMeters")?.numericValue?.toFloat() ?: 0f,
        file.findGlobalAttribute("VolumeCoveragePattern")?.numericValue?.toInt() ?: -1,
        file.findGlobalAttribute("VolumeCoveragePatternName")?.stringValue ?: "UNKNOWN",
        file.findGlobalAttribute("Title")?.stringValue ?: "UNKNOWN"
    )


    val timeCoverageStart: String =
        file.findGlobalAttribute("time_coverage_start")?.stringValue ?: "UNKNOWN" //should convert to dates later
    val timeCoverageEnd: String = file.findGlobalAttribute("time_coverage_end")?.stringValue ?: "UNKNOWN"

    val summary: String = file.findGlobalAttribute("Summary")?.stringValue ?: "UNKNOWN"

    val vcp: Int = file.findGlobalAttribute("VolumeCoveragePattern")?.numericValue?.toInt() ?: -1
    val vcpName: String = interpretVCP(vcp.toString())

    private val productVolumes: EnumMap<Product, RadarProductVolume> = EnumMap(meteo.radar.Product::class.java)

    private fun interpretVCP(vcp: String): String {
        if(vcp == "215") return "General Surveillance"
        else if(vcp == "212") return "Precip"
        else if(vcp.length == 2 && vcp.startsWith("1")) {
            return "Convection"
        } else if(vcp.length == 2 && vcp.startsWith("2")) {
            return "Precip"
        } else if(vcp.length == 3 && vcp.startsWith("1")) {
            return "MPDA"
        } else if(vcp.length == 2 && vcp.startsWith("3")) {
            return "Clear-Air"
        } else {
            return "Unknown"
        }
    }

    init {
        val startTime = System.currentTimeMillis()
        for (product in Product.entries) {
            try {
                val productVolume = RadarProductVolume(file, handle, station, product)
                productVolumes[product] = productVolume
            } catch (e: Exception) {
                println("Unable to load product from file. Product: $product")
            }
        }
        val dur = System.currentTimeMillis() - startTime
        val durSec = dur / 1000.0f
        println("time to process volume: $durSec")
    }

    fun getProductVolume(product: Product): RadarProductVolume? {
        return productVolumes[product]
    }
}