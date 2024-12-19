package meteo.radar

import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile

class RadarProductVolume(file: NetcdfFile, val product: Product) {
    val scans: ArrayList<RadarScan<Float>> = arrayListOf()

    val station: String = file.findGlobalAttribute("Station")?.stringValue ?: "UNKNOWN"
    val stationName: String = file.findGlobalAttribute("StationName")?.stringValue ?: "UNKNOWN"

    val latitude: Float = file.findGlobalAttribute("StationLatitude")?.numericValue?.toFloat() ?: 0f
    val longitude: Float = file.findGlobalAttribute("StationLongitude")?.numericValue?.toFloat() ?: 0f
    val elevationMeters: Float = file.findGlobalAttribute("StationElevationInMeters")?.numericValue?.toFloat() ?: 0f

    val timeCoverageStart: String =
        file.findGlobalAttribute("time_coverage_start")?.stringValue ?: "UNKNOWN" //should convert to dates later
    val timeCoverageEnd: String = file.findGlobalAttribute("time_coverage_end")?.stringValue ?: "UNKNOWN"

    val title: String = file.findGlobalAttribute("Title")?.stringValue ?: "UNKNOWN"
    val summary: String = file.findGlobalAttribute("Summary")?.stringValue ?: "UNKNOWN"

    val vcp: Int = file.findGlobalAttribute("VolumeCoveragePattern")?.numericValue?.toInt() ?: -1
    val vcpName: String = file.findGlobalAttribute("VolumeCoveragePatternName")?.stringValue ?: "UNKNOWN"

    init {
        val azimuthVar = file.findVariable(product.azimuthField)
        val elevationVar = file.findVariable(product.elevationField)
        val rangeVar = file.findVariable(product.distanceField)
        val variableVar = file.findVariable(product.dataField)

        if (azimuthVar == null || elevationVar == null || rangeVar == null || variableVar == null) {
            throw Exception("Unable to read product data from file. Product: $product")
        }

        println(variableVar.dataType.signedness)

        val addOffset = variableVar.attributes().findAttributeDouble("add_offset", 0.0).toFloat()
        val scale = variableVar.attributes().findAttributeDouble("scale_factor", 1.0).toFloat()
        println(addOffset)
        println(scale)

        val belowThreshold: Byte = 0;
        val noData: Byte = 1;

        println("title: ${title}")

        val productData: ArrayByte.D3 = variableVar.read() as ArrayByte.D3
        val azimuthData: ArrayFloat.D2 = azimuthVar.read() as ArrayFloat.D2
        val elevationData: ArrayFloat.D2 = elevationVar.read() as ArrayFloat.D2
        val rangeData: ArrayFloat.D1 = rangeVar.read() as ArrayFloat.D1

        val shape = productData.shape

        for (sweep in 0..<shape[0]) { //Sweeps
//            println("---SWEEP $sweep---")
            val scan: ArrayList<List<RadarGate<Float>>> = arrayListOf()
            var elevation = 0f;
            for (radial in 0..<shape[1]) { //Sweeps
                val azimuth = azimuthData.get(sweep, radial)
                elevation = elevationData.get(sweep, radial)

                val gates: ArrayList<RadarGate<Float>> = arrayListOf()
                for (gate in 0..<shape[2]) { //Sweeps
                    val range = rangeData.get(gate)
                    val rawValue = productData.get(sweep, radial, gate).toUByte().toFloat()

                    if (rawValue != belowThreshold.toFloat() && rawValue != noData.toFloat()) {
                        val scaledValue = (rawValue * scale) + addOffset
//                        println(if (scaledValue > 10) scaledValue else "")
//                        println("Azimuth: $azimuth, Range: $range | Data: $scaledValue")
//                        println(scaledValue)
                        gates.add(RadarGate<Float>(elevation, azimuth, range, scaledValue))
                    }
                }
                scan.add(gates)
            }

            scans.add(RadarScan(elevation, scan))
        }
    }
}