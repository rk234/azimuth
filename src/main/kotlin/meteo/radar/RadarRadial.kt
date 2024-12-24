package meteo.radar

import java.nio.IntBuffer

data class RadarRadial(val azimuth: Float, val gates: IntBuffer) {
    constructor(azimuth: Float, gates: List<RadarGate>) : this (azimuth, gatesToBuffer(gates))

    private companion object {
        fun gatesToBuffer(gates: List<RadarGate>): IntBuffer {
            val buf = IntBuffer.allocate(gates.size)
            gates.forEach { gate -> buf.put(gate.packedData)}
            buf.compact()
            return buf
        }
    }
}