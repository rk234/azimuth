package data.state

import java.nio.file.Paths
import java.util.prefs.Preferences
import kotlin.io.path.absolutePathString

object UserPrefs {
    private const val NUM_LOOP_FRAMES_KEY: String = "numLoopFrames"
    private const val DEFAULT_STATION_KEY: String = "numLoopFrames"
    private const val RADAR_AUTO_POLL_FREQ_KEY: String = "autoPollFrequency"
    private const val RADAR_CACHE_PATH_KEY: String = "radarCachePath"

    private const val DEFAULT_NUM_LOOP_FRAMES: Int = 5
    private const val DEFAULT_STATION: String = "KLWX"
    private const val DEFAULT_RADAR_AUTO_POLL_FREQ_SEC: Int = 10
    private val DEFAULT_RADAR_CACHE_PATH: String = Paths.get(System.getProperty("user.home"),  ".azimuth", "cache", "radar").absolutePathString()

    private val prefs = Preferences.userRoot()

    fun reset() {
        setNumLoopFrames(DEFAULT_NUM_LOOP_FRAMES)
        setDefaultStation(DEFAULT_STATION)
        setAutoPollFrequency(DEFAULT_RADAR_AUTO_POLL_FREQ_SEC)
    }

    fun numLoopFrames(): Int = prefs.getInt(NUM_LOOP_FRAMES_KEY, DEFAULT_NUM_LOOP_FRAMES)
    fun defaultStation(): String = prefs.get(DEFAULT_STATION_KEY, DEFAULT_STATION)
    fun radarAutoPollFrequencySec(): Int = prefs.getInt(RADAR_AUTO_POLL_FREQ_KEY, DEFAULT_RADAR_AUTO_POLL_FREQ_SEC)
    fun radarCachePath(): String = prefs.get(RADAR_CACHE_PATH_KEY, DEFAULT_RADAR_CACHE_PATH)

    fun setNumLoopFrames(num: Int) = prefs.putInt(NUM_LOOP_FRAMES_KEY, num)
    fun setDefaultStation(station: String) = prefs.put(NUM_LOOP_FRAMES_KEY, station)
    fun setAutoPollFrequency(freqMs: Int) = prefs.putInt(RADAR_AUTO_POLL_FREQ_KEY, freqMs)
    fun setRadarCachePath(path: String) = prefs.put(RADAR_CACHE_PATH_KEY, path)
}