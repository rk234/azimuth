package data.warnings

import java.time.ZonedDateTime

data class Warning(
    val type: String,
    val message: String,
    val areaDesc: String,
    val sent: ZonedDateTime,
    val effective: ZonedDateTime,
    val onset: ZonedDateTime,
    val expires: ZonedDateTime,
) {
}