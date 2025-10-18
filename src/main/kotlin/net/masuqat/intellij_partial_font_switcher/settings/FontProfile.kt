package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme

abstract class FontProfile {
    val scheme = createScheme()

    protected fun createScheme(): EditorColorsScheme {
        val globalScheme = EditorColorsManager.getInstance().globalScheme
        val scheme = globalScheme.clone() as EditorColorsScheme
        scheme.fontPreferences = globalScheme.fontPreferences // to be editable
        return scheme
    }
}