import meteo.radar.Product
import meteo.radar.RadarGate
import meteo.radar.RadarScan
import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile
import java.util.Date

class RadarVolume(file: NetcdfFile, val product: Product) {
    val scans: ArrayList<RadarScan<Float>> = arrayListOf()

    val station: String
    val stationName: String

    val latitude: Float
    val longitude: Float
    val elevation: Float

    val timeCoverageStart: String //should convert to dates later
    val timeCoverageEnd: String

    val title: String
    val summary: String

    val vcp: Int
    val vcpName: String

    init {
        println(file)
        station = file.findGlobalAttribute("Station")?.stringValue ?: "UNKNOWN"
        stationName = file.findGlobalAttribute("StationName")?.stringValue ?: "UNKNOWN"

        latitude = file.findGlobalAttribute("StationLatitude")?.numericValue?.toFloat() ?: 0f
        longitude = file.findGlobalAttribute("StationLongitude")?.numericValue?.toFloat() ?: 0f
        elevation = file.findGlobalAttribute("StationElevationInMeters")?.numericValue?.toFloat() ?: 0f

        timeCoverageStart = file.findGlobalAttribute("time_coverage_start")?.stringValue ?: "UNKNOWN"
        timeCoverageEnd = file.findGlobalAttribute("time_coverage_end")?.stringValue ?: "UNKNOWN"

        title = file.findGlobalAttribute("Title")?.stringValue ?: "UNKNOWN"
        summary = file.findGlobalAttribute("Summary")?.stringValue ?: "UNKNOWN"

        vcp = file.findGlobalAttribute("VolumeCoveragePattern")?.numericValue?.toInt() ?: -1
        vcpName = file.findGlobalAttribute("VolumeCoveragePatternName")?.stringValue ?: "UNKNOWN"

        val azimuthVar = file.findVariable(product.azimuthField)
        val elevationVar = file.findVariable(product.elevationField)
        val rangeVar = file.findVariable(product.distanceField)
        val variableVar = file.findVariable(product.dataField)

        if (azimuthVar == null || elevationVar == null || rangeVar == null || variableVar == null) {
            throw Exception("Unable to read product data from file. Product: $product")
        }

        println(rangeVar)
        println(azimuthVar)

        val addOffset = variableVar.attributes().findAttributeDouble("add_offset", 0.0).toFloat()
        val scale = variableVar.attributes().findAttributeDouble("scale_factor", 1.0).toFloat()

        val belowThreshold: Byte = 0;
        val noData: Byte = 1;

        println("${product.dataField} shape ${variableVar.shape.contentToString()}")
        println("${product.distanceField} shape ${rangeVar.shape.contentToString()}")
        println("${product.azimuthField} shape ${azimuthVar.shape.contentToString()}")

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
                    val rawValue = productData.get(sweep, radial, gate)

                    if (rawValue != belowThreshold && rawValue != noData) {
                        val scaledValue = (productData.get(sweep, radial, gate) * scale) + addOffset
//                        println("Azimuth: $azimuth, Range: $range | Data: $scaledValue")
                        gates.add(RadarGate<Float>(elevation, azimuth, range, scaledValue))
                    }
                }
                scan.add(gates)
            }

            scans.add(RadarScan(elevation, scan))
        }
    }
}