package data.state

import java.util.prefs.Preferences

object UserPrefs {
    private val NUM_LOOP_FRAMES_KEY: String = "numLoopFrames"
    private val DEFAULT_STATION_KEY: String = "numLoopFrames"
    private val AUTO_POLL_FREQ_KEY: String = "autoPollFrequency"

    private val DEFAULT_NUM_LOOP_FRAMES: Int = 5
    private val DEFAULT_STATION: String = "KLWX"
    private val DEFAULT_AUTO_POLL_FREQ: Long = 60*1000

    private val prefs = Preferences.userRoot()

    fun reset() {
        setNumLoopFrames(DEFAULT_NUM_LOOP_FRAMES)
        setDefaultStation(DEFAULT_STATION)
        setAutoPollFrequency(DEFAULT_AUTO_POLL_FREQ)
    }

    fun numLoopFrames(): Int = prefs.getInt(NUM_LOOP_FRAMES_KEY, DEFAULT_NUM_LOOP_FRAMES)
    fun defaultStation(): String = prefs.get(DEFAULT_STATION_KEY, DEFAULT_STATION)
    fun autoPollFrequency(): Long = prefs.getLong(AUTO_POLL_FREQ_KEY, DEFAULT_AUTO_POLL_FREQ)

    fun setNumLoopFrames(num: Int) = prefs.putInt(NUM_LOOP_FRAMES_KEY, num)
    fun setDefaultStation(station: String) = prefs.put(NUM_LOOP_FRAMES_KEY, station)
    fun setAutoPollFrequency(freqMs: Long) = prefs.putLong(AUTO_POLL_FREQ_KEY, freqMs)
}