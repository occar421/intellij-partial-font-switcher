package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.observable.properties.GraphProperty

abstract class FontProfile(val enabled: GraphProperty<Boolean>, val scheme: EditorColorsScheme) {
    companion object {
        fun createInitialScheme(): EditorColorsScheme {
            val globalScheme = EditorColorsManager.getInstance().globalScheme
            val scheme = globalScheme.clone() as EditorColorsScheme
            scheme.fontPreferences = globalScheme.fontPreferences // to be editable
            return scheme
        }
    }
}