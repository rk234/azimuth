package utils

fun interface ProgressListener {
    fun onProgress(progress: Double?, message: String)
}