package rendering

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL45.GL_LINEAR
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import java.nio.ByteBuffer
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * Glyph metrics for a single character in the SDF atlas.
 */
data class GlyphInfo(
    val char: Char,
    val u0: Float, val v0: Float,   // Top-left UV in atlas
    val u1: Float, val v1: Float,   // Bottom-right UV in atlas
    val width: Int, val height: Int, // Glyph dimensions in pixels
    val xOffset: Int, val yOffset: Int, // Offset from cursor to top-left of glyph
    val advance: Float               // Horizontal advance to next character
)

/**
 * Font atlas using SDF (Signed Distance Field) rendering via STB TrueType.
 *
 * @param fontPath Path to the TTF font file
 * @param fontHeightPixels Desired font height in pixels
 * @param atlasWidth Width of the atlas texture
 * @param atlasHeight Height of the atlas texture
 * @param sdfPadding Padding around each glyph for SDF (default 5)
 * @param sdfOnEdgeValue The SDF value at the glyph edge (0-255), default 128
 * @param sdfPixelDistScale Scale for the distance field (default 64.0 / sdfPadding)
 */
class FontAtlas(
    fontPath: String,
    private val fontHeightPixels: Float,
    private val atlasWidth: Int,
    private val atlasHeight: Int,
    private val sdfPadding: Int = 5,
    private val sdfOnEdgeValue: Int = 128,
    private val sdfPixelDistScale: Float = 64.0f / sdfPadding
) {
    private val fontInfo: STBTTFontinfo = STBTTFontinfo.create()
    private val ttfBuffer: ByteBuffer
    private val bitmap: ByteBuffer

    private val firstChar = 32
    private val charCount = 96

    private val glyphs: MutableMap<Char, GlyphInfo> = mutableMapOf()

    val scale: Float
    val ascent: Float
    val descent: Float
    val lineGap: Float

    val texture: Texture2D by lazy { createTexture() }

    init {
        // Load font file into a direct ByteBuffer (required by STB)
        val fontBytes = Files.readAllBytes(Path(fontPath))
        ttfBuffer = MemoryUtil.memAlloc(fontBytes.size)
        ttfBuffer.put(fontBytes).flip()

        if (!STBTruetype.stbtt_InitFont(fontInfo, ttfBuffer)) {
            throw RuntimeException("Failed to initialize font: $fontPath")
        }

        // Calculate scale to map font units to desired pixel height
        scale = STBTruetype.stbtt_ScaleForPixelHeight(fontInfo, fontHeightPixels)

        // Get font vertical metrics (in font units, scale to pixels)
        MemoryStack.stackPush().use { stack ->
            val pAscent = stack.mallocInt(1)
            val pDescent = stack.mallocInt(1)
            val pLineGap = stack.mallocInt(1)
            STBTruetype.stbtt_GetFontVMetrics(fontInfo, pAscent, pDescent, pLineGap)
            ascent = pAscent.get(0) * scale
            descent = pDescent.get(0) * scale
            lineGap = pLineGap.get(0) * scale
        }

        // Allocate atlas bitmap
        bitmap = BufferUtils.createByteBuffer(atlasWidth * atlasHeight)

        // Bake SDF glyphs into atlas
        bakeSdfGlyphs()
    }

    private fun bakeSdfGlyphs() {
        var cursorX = 0
        var cursorY = 0
        var rowHeight = 0

        MemoryStack.stackPush().use { stack ->
            val pWidth = stack.mallocInt(1) // width of the glyph
            val pHeight = stack.mallocInt(1) // height of the glyph
            val pXoff = stack.mallocInt(1) // x offset from cursor
            val pYoff = stack.mallocInt(1) // y offset from cursor
            val pAdvance = stack.mallocInt(1) // how much to move cursor after drawing
            val pLeftBearing = stack.mallocInt(1) // offset from cursor to left edge

            for (i in 0 until charCount) {
                val char: Char = (firstChar + i).toChar()

                // Get horizontal metrics for advance
                STBTruetype.stbtt_GetCodepointHMetrics(fontInfo, char.code, pAdvance, pLeftBearing)
                val advance = pAdvance.get(0) * scale

                // Generate SDF bitmap for this glyph
                val sdfBitmap: ByteBuffer? = STBTruetype.stbtt_GetCodepointSDF(
                    fontInfo,
                    scale,
                    char.code,
                    sdfPadding,
                    sdfOnEdgeValue.toByte(),
                    sdfPixelDistScale,
                    pWidth,
                    pHeight,
                    pXoff,
                    pYoff
                )

                val glyphW = pWidth.get(0)
                val glyphH = pHeight.get(0)
                val xOff = pXoff.get(0)
                val yOff = pYoff.get(0)

                if (sdfBitmap == null || glyphW == 0 || glyphH == 0) {
                    // Whitespace or empty glyph — store metrics with zero-size UV
                    glyphs[char] = GlyphInfo(
                        char = char,
                        u0 = 0f, v0 = 0f, u1 = 0f, v1 = 0f,
                        width = 0, height = 0,
                        xOffset = 0, yOffset = 0,
                        advance = advance
                    )
                    continue
                }

                // Check if glyph fits in current row; if not, move to next row
                if (cursorX + glyphW > atlasWidth) {
                    cursorX = 0
                    cursorY += rowHeight
                    rowHeight = 0
                }

                // Check if we've run out of vertical space
                if (cursorY + glyphH > atlasHeight) {
                    STBTruetype.stbtt_FreeSDF(sdfBitmap)
                    throw RuntimeException("Font atlas too small to fit all glyphs")
                }

                // Copy glyph SDF data into atlas bitmap
                for (row in 0 until glyphH) {
                    for (col in 0 until glyphW) {
                        val srcIndex = row * glyphW + col
                        val dstIndex = (cursorY + row) * atlasWidth + (cursorX + col)
                        bitmap.put(dstIndex, sdfBitmap.get(srcIndex))
                    }
                }

                // Record glyph info with UV coordinates
                glyphs[char] = GlyphInfo(
                    char = char,
                    u0 = cursorX.toFloat() / atlasWidth,
                    v0 = cursorY.toFloat() / atlasHeight,
                    u1 = (cursorX + glyphW).toFloat() / atlasWidth,
                    v1 = (cursorY + glyphH).toFloat() / atlasHeight,
                    width = glyphW,
                    height = glyphH,
                    xOffset = xOff,
                    yOffset = yOff,
                    advance = advance
                )

                // Advance cursor
                cursorX += glyphW + 1 // +1 pixel gap to avoid bleeding
                rowHeight = maxOf(rowHeight, glyphH + 1)

                STBTruetype.stbtt_FreeSDF(sdfBitmap)
            }
        }
    }

    private fun createTexture(): Texture2D {
        val tex = Texture2D()
        tex.bind()
        tex.uploadData(atlasWidth, atlasHeight, bitmap)
        tex.setParameters(GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE)
        return tex
    }

    /**
     * Get glyph info for a character. Returns null if character is not in atlas.
     */
    fun getGlyph(c: Char): GlyphInfo? {
        return glyphs[c]
    }

    /**
     * Get glyph info, falling back to '?' if character is not found.
     */
    fun getGlyphOrDefault(c: Char): GlyphInfo {
        return glyphs[c] ?: glyphs['?'] ?: error("Default glyph '?' not found in atlas")
    }

    fun stringWidth(text: String): Float {
        var width = 0f
        for (char in text) {
            val glyph = getGlyph(char) ?: continue
            width += glyph.advance
        }
        return width
    }

    fun destroy() {
        texture.destroy()
        MemoryUtil.memFree(ttfBuffer)
    }

    /**
     * Write the SDF atlas bitmap to a PNG file for debugging/visualization.
     * @param outputPath Path to write the PNG file
     */
    fun writeBitmapToFile(outputPath: String) {
        org.lwjgl.stb.STBImageWrite.stbi_write_png(
            outputPath,
            atlasWidth,
            atlasHeight,
            1, // 1 component (grayscale)
            bitmap,
            atlasWidth // stride in bytes
        )
    }
}