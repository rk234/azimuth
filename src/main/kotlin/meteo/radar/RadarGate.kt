package meteo.radar

@JvmInline
value class RadarGate(val packedData: Int) {
    fun idx() = (0xFFFF and (packedData shr 8)).toUShort()
    fun data() = (0xFF and packedData).toUByte()

    fun scaledValue(scale: Float, addOffset: Float) = (data().toFloat() * scale)+addOffset

    companion object {
        fun pack(idx: UShort, data: UByte): RadarGate {
            val packed = (data.toInt() and 0xFF) or ((idx.toInt() shl 8))
            return RadarGate(packed)
        }
    }
}