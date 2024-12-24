package meteo.radar

@JvmInline
value class VolumeFileHandle(val fileName: String) {
    fun station(): String {
        return fileName.substringBefore("_")
    }

    override fun toString(): String {
        return fileName
    }
}