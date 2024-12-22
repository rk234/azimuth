package meteo.radar

import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile
import kotlin.math.max

class RadarVolume(file: NetcdfFile) {
    private class ProductMetadata(file: NetcdfFile, product: Product) {
        val azimuthVar = file.findVariable(product.azimuthField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val elevationVar = file.findVariable(product.elevationField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val rangeVar = file.findVariable(product.distanceField) ?: throw Exception("Unable to read product meta data from file. Product: $product")
        val variableVar = file.findVariable(product.dataField) ?: throw Exception("Unable to read product meta data from file. Product: $product")

        val addOffset = variableVar.attributes().findAttributeDouble("add_offset", 0.0).toFloat()
        val scale = variableVar.attributes().findAttributeDouble("scale_factor", 1.0).toFloat()

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

    val productVolumes: HashMap<Product, RadarProductVolume> = hashMapOf()

    private fun interpretVCP(vcp: String): String {
        if(vcp == "215") return "General Surveillance"
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
//        val map = mutableMapOf<Product, ProductMetadata>()
//        var sweeps: Int = 0
//        var radials: Int = 0
//        var gates: Int = 0
//
//        for (product in Product.entries) {
//            try {
//                map[product] = ProductMetadata(file, product)
//                sweeps = max(sweeps, map[product]!!.numSweeps)
//                radials = max(radials, map[product]!!.numRadials)
//                gates = max(gates, map[product]!!.numGates)
//            } catch (e: Exception) {
//                println("Unable to load product from file. Product: $product")
//                println("Exception: $e")
//            }
//        }
//
//        for(sweep in 0..<sweeps) {
//            for(radial in 0..<radials) {
//                for(gate in 0..<gates) {
//
//                }
//            }
//        }
//

        for (product in Product.entries) {
            try {
                val productVolume = RadarProductVolume(file, timeCoverageEnd, station, product)
                productVolumes[product] = productVolume
            } catch (e: Exception) {
                println("Unable to load product from file. Product: $product")
            }
        }
    }

    fun getProductVolume(product: Product): RadarProductVolume? {
        return productVolumes[product]
    }
}