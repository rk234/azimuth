package meteo.radar

import ucar.ma2.ArrayByte
import ucar.ma2.ArrayFloat
import ucar.nc2.NetcdfFile

class RadarVolume(file: NetcdfFile, product: Product) {
    val scans: List<RadarScan<Float>> = listOf()
    val product: Product

    init {
        this.product = product

        val azimuthVar = file.findVariable(product.azimuthField)
        val elevationVar = file.findVariable(product.elevationField)
        val rangeVar = file.findVariable(product.distanceField)
        val variableVar = file.findVariable(product.dataField)

        if (azimuthVar == null || elevationVar == null || rangeVar == null || variableVar == null) {
            throw Exception("Unable to read product data from file. Product: $product")
        }

        println("${product.dataField} shape ${variableVar?.shape.contentToString()}")
        println("${product.distanceField} shape ${rangeVar?.shape.contentToString()}")
        println("${product.azimuthField} shape ${azimuthVar?.shape.contentToString()}")

        val productData: ArrayByte.D3 = variableVar.read() as ArrayByte.D3
        val azimuthData: ArrayFloat.D2 = azimuthVar.read() as ArrayFloat.D2
//        val elevationData: ArrayFloat.D3 = elevationVar.read() as ArrayFloat.D3
        val rangeData: ArrayFloat.D1 = rangeVar.read() as ArrayFloat.D1

        val shape = productData.shape

        for (sweep in 0..<shape[0]) { //Sweeps
            println("---SWEEP $sweep---")
            for (radial in 0..<shape[1]) { //Sweeps
                val azimuth = azimuthData.get(sweep, radial)
//                val elevation = elevationData.getFloat(sweep, radiall)
                for (gate in 0..<shape[2]) { //Sweeps
                    val range = rangeData.get(gate)
                    val gateValue = productData.get(sweep, radial, gate)
                    println("Azimuth: $azimuth, Range: $range | Data: $gateValue")
                }
            }
        }
    }
}