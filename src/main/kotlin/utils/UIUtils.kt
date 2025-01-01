package utils

import com.formdev.flatlaf.extras.FlatSVGIcon
import java.awt.Color

fun loadIcon(name: String, width: Int, height: Int): FlatSVGIcon {
    val icon = FlatSVGIcon("icons/$name", width, height)
    icon.colorFilter = FlatSVGIcon.ColorFilter()
    icon.colorFilter.add(Color.BLACK, Color.BLACK, Color.LIGHT_GRAY)
    return icon
}
