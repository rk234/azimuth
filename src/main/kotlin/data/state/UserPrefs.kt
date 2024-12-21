package data.state

import java.util.prefs.Preferences

object UserPrefs {
    private val NUM_LOOP_FRAMES_KEY: String = "numLoopFrames"
    private val DEFAULT_STATION_KEY: String = "numLoopFrames"

    private val DEFAULT_NUM_LOOP_FRAMES: Int = 10
    private val DEFAULT_STATION: String = "KLWX"

    private val prefs = Preferences.userRoot()

    fun numLoopFrames(): Int = prefs.getInt(NUM_LOOP_FRAMES_KEY, DEFAULT_NUM_LOOP_FRAMES)
    fun defaultStation(): String = prefs.get(DEFAULT_STATION_KEY, DEFAULT_STATION)

    fun setNumLoopFrames(num: Int) = prefs.putInt(NUM_LOOP_FRAMES_KEY, num)
    fun setDefaultStation(station: String) = prefs.put(NUM_LOOP_FRAMES_KEY, station)
}