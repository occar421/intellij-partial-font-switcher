package net.masuqat.intellij_partial_font_switcher.language_level

import com.intellij.openapi.fileTypes.FileType

class LanguageFontSelector {
    fun select(fileType: FileType): String {
        // TODO
        if (fileType.name != "Kotlin") {
            return "Impact"
        }

        return defaultFontName
    }

    val defaultFontName: String
        get() = "Courier New" // TODO
}