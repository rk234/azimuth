package meteo.radar

import ucar.nc2.NetcdfFile

class RadarVolume(file: NetcdfFile) {
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