package utils

fun interface ProgressListener {
    fun notifyProgress(progress: Double?, message: String)
}