package views

import meteo.radar.Colormap
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.Graphics
import java.nio.ByteBuffer
import javax.swing.JPanel

class ColormapBar(val colormap: Colormap, val steps: Int) : JPanel() {

    private val cmapData: ByteBuffer;

    init {
        cmapData = MemoryUtil.memAlloc(steps * 3)
        colormap.genTextureData(steps, cmapData)
    }

    override fun paintComponent(g: Graphics?) {
        super.paintComponent(g)
        val dx = width.toFloat() / steps
        for(i in 0..<steps) {
            val pixel = i * 3
            g?.color = Color(
                cmapData.get(pixel).toUByte().toInt(),
                cmapData.get(pixel + 1).toUByte().toInt(),
                cmapData.get(pixel + 2).toUByte().toInt(),
            )
            g?.fillRect((dx*i).toInt(), 0, (dx*(i+1)).toInt(), height)
        }
    }
}