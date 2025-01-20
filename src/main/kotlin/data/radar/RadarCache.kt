package data.radar

import ucar.nc2.util.DiskCache
import java.sql.Date

object RadarCache {
    fun init(dir: String) {
        setDirectory(dir)
        makeDirectory()
        DiskCache.setCachePolicy(true)
    }

    fun makeDirectory() {
        DiskCache.makeRootDirectory()
    }

    fun setDirectory(dir: String) {
        DiskCache.setRootDirectory(dir)
    }

    fun clearCache(maxBytes: Long, sbuff: StringBuilder) {
        DiskCache.cleanCache(maxBytes, sbuff)
    }

    fun clearCacheSince(date: Date, sbuff: StringBuilder) {
        DiskCache.cleanCache(date, sbuff)
    }
}