package net.masuqat.intellij_partial_font_switcher.file_type_level

import com.intellij.openapi.editor.Editor

val selector = FileTypeFontPreferenceSelector()

fun Editor.overrideWithFileTypeFont() {
    if (this.virtualFile != null) {
        this.colorsScheme.fontPreferences = selector.select(this.virtualFile.fileType)
    }
}

fun Editor.revertFileTypeFont() {
    this.colorsScheme.fontPreferences = selector.globalFontPreference
}