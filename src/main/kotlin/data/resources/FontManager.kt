package data.resources

import rendering.FontAtlas


class FontManager {
    private val fonts = mutableMapOf<String, MutableMap<Float, FontAtlas>>()
    private val ATLAS_WIDTH = 1024
    private val ATLAS_HEIGHT = 1024
    companion object {
        val instance = FontManager()

        fun init() {
            instance.loadFont("IBMPlexSans-Condensed_Bold", "src/main/resources/fonts/IBMPlexSans_Condensed-Bold.ttf")
        }
    }

    fun getDefaultFont(): FontAtlas {
        return getFont("IBMPlexSans-Condensed_Bold", 150f)!!
    }

    fun getFont(name: String, size: Float): FontAtlas? {
        return fonts[name]?.get(size)
    }

    fun loadFont(name: String, path: String, size: Float = 150f, atlasWidth: Int = ATLAS_WIDTH, atlasHeight: Int = ATLAS_HEIGHT): FontAtlas {
        val atlas = FontAtlas(path, size, atlasWidth, atlasHeight)
        if (!fonts.containsKey(name)) {
            fonts[name] = mutableMapOf()
        }
        fonts[name]!![size] = atlas
        return atlas
    }
}