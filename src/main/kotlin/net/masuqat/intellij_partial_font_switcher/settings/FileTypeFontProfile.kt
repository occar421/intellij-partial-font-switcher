package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsScheme
import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class FileTypeFontProfile(val fileTypeName: String, enabled: Boolean, scheme: EditorColorsScheme) :
    FontProfile(enabled, scheme) {
    val isBaseProfile: Boolean
        get() = fileTypeName == AppSettings.BASE_FILE_TYPE_NAME
}