package meteo.radar

import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile

class RadarProductVolume(file: NetcdfFile, val timeCoverageEnd: String, val station: Station, val product: Product) {
    val scans: ArrayList<RadarSweep> = arrayListOf()

    init {
//        println(file.variables)
        val azimuthVar = file.findVariable(product.azimuthField)
        val elevationVar = file.findVariable(product.elevationField)
        val rangeVar = file.findVariable(product.distanceField)
        val variableVar = file.findVariable(product.dataField)


        if (azimuthVar == null || elevationVar == null || rangeVar == null || variableVar == null) {
            throw Exception("Unable to read product data from file. Product: $product")
        }

//        println(variableVar.dataType.signedness)

        val addOffset = variableVar.attributes().findAttributeDouble("add_offset", 0.0).toFloat()
        val scale = variableVar.attributes().findAttributeDouble("scale_factor", 1.0).toFloat()
//        println(addOffset)
//        println(scale)

//        val belowThreshold: Byte = 0;
        val belowThreshold: Float = variableVar.attributes().findAttribute("signal_below_threshold")?.numericValue?.toFloat() ?: 0.0f
        val noData: Float = variableVar.attributes().findAttribute("missing_value")?.numericValue?.toFloat() ?: 0.0f

        val productData: ArrayByte.D3 = variableVar.read() as ArrayByte.D3
        val azimuthData: ArrayFloat.D2 = azimuthVar.read() as ArrayFloat.D2
        val elevationData: ArrayFloat.D2 = elevationVar.read() as ArrayFloat.D2
        val rangeData: ArrayFloat.D1 = rangeVar.read() as ArrayFloat.D1

        val shape = productData.shape

        val numSweeps = shape[0]
        val numRadials = shape[1]
        val numGates = shape[2]

        for (sweep in 0..<numSweeps) { //Sweeps
//            println("---SWEEP $sweep---")
            val scan: ArrayList<RadarRadial> = arrayListOf()
            var elevation = 0f;
            for (radial in 0..<numRadials) { //Radials
                val azimuth = azimuthData.get(sweep, radial)
                elevation = elevationData.get(sweep, radial)

                val gates: ArrayList<RadarGate> = arrayListOf()
                for (gate in 0..<numGates) { //Gates
                    val rawValue = productData.get(sweep, radial, gate).toUByte()

                    if (rawValue.toFloat() != belowThreshold && rawValue.toFloat() != noData) {
//                        val scaledValue = (rawValue.toFloat() * scale) + addOffset
//                        println(if (scaledValue > 10) scaledValue else "")
//                        println("Azimuth: $azimuth, Range: $range | Data: $scaledValue")
//                        println(scaledValue)
                        gates.add(RadarGate.pack(gate.toUShort(), rawValue))
                    }
                }
                scan.add(RadarRadial(azimuth, gates))
            }

            scans.add(RadarSweep(elevation, scan, station, product, numRadials, numGates, rangeData.get(0), rangeData.get(1)-rangeData.get(0), scale, addOffset))
        }
    }
}