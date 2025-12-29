package net.masuqat.intellij_partial_font_switcher.editor_mod

import com.intellij.openapi.editor.Editor

val selector = FileTypeFontPreferenceSelector()

fun Editor.switchFontPreference() {
    if (this.virtualFile != null) {
        val fontPreferences = selector.select(this.virtualFile!!.fileType)
        this.colorsScheme.fontPreferences = fontPreferences
        this.colorsScheme.lineSpacing = fontPreferences.lineSpacing
    }
}

fun Editor.revertFontPreference() {
    this.colorsScheme.fontPreferences = selector.globalFontPreference
    this.colorsScheme.lineSpacing = selector.globalFontPreference.lineSpacing
}
