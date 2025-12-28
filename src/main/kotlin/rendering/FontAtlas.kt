package rendering

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL45.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL45.GL_LINEAR
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTTFontinfo
import org.lwjgl.stb.STBTruetype
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.file.Files
import kotlin.io.path.Path


class FontAtlas(
    fontPath: String,
    fontSize: Float,
    private val bitmapWidth: Int,
    private val bitmapHeight: Int
) {
    private val ascent: Int // The maximum distance above the baseline that any glyph extends.
    private val descent: Int // The maximum distance below the baseline that any glyph extends (typically negative).
    private val lineGap: Int // The additional space between lines of text.

    private val ttfBuffer: ByteBuffer
    private val bitmap: ByteBuffer

    private val firstChar = 32
    private val charCount = 96
    private val bakedChars: STBTTBakedChar.Buffer = STBTTBakedChar.malloc(charCount)

    val texture: Texture2D by lazy {
        createTexture()
    }

    init {
        val info = STBTTFontinfo.create()
        ttfBuffer = ByteBuffer.wrap(Files.readAllBytes(Path(fontPath)))

        if (!STBTruetype.stbtt_InitFont(info, ttfBuffer)) {
            throw RuntimeException("Failed to initialize font information for font at path: $fontPath")
        }

        MemoryStack.stackPush().use { stack ->
            val pAscent: IntBuffer = stack.mallocInt(1)
            val pDescent: IntBuffer = stack.mallocInt(1)
            val pLineGap: IntBuffer = stack.mallocInt(1)

            STBTruetype.stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)

            ascent = pAscent.get(0)
            descent = pDescent.get(0)
            lineGap = pLineGap.get(0)
        }

        bitmap = BufferUtils.createByteBuffer(bitmapWidth * bitmapHeight)
        val bakedChars = STBTTBakedChar.malloc(charCount)

        STBTruetype.stbtt_BakeFontBitmap(
            ttfBuffer,
            fontSize,
            bitmap,
            bitmapWidth,
            bitmapHeight,
            firstChar,
            bakedChars
        )
    }

    private fun createTexture(): Texture2D {
        val texture = Texture2D()
        texture.bind()
        texture.uploadData(bitmapWidth, bitmapHeight, bitmap)
        texture.setParameters(GL_LINEAR, GL_LINEAR, GL_CLAMP_TO_EDGE, GL_CLAMP_TO_EDGE)
        return texture
    }

    fun bakedChar(c: Char): STBTTBakedChar {
        if (c.code < firstChar || c.code >= firstChar + charCount) {
            throw IllegalArgumentException("Character $c is out of bounds for this font atlas.")
        }
        return bakedChars.get(c.code - firstChar)
    }

    fun destroy() {
        texture.destroy()
        bakedChars.free()
    }
}