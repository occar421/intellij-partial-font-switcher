package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme

class ElementTypeFontProfile(val elementTypeName: String) {
    val scheme = createScheme()

    private fun createScheme(): EditorColorsScheme {
        val globalScheme = EditorColorsManager.getInstance().globalScheme
        val scheme = globalScheme.clone() as EditorColorsScheme
        scheme.fontPreferences = globalScheme.fontPreferences // to be editable
        return scheme
    }
}