package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.observable.properties.GraphProperty
import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class FileTypeFontProfile(
    val fileTypeName: GraphProperty<String>,
    enabled: GraphProperty<Boolean>,
    scheme: EditorColorsScheme
) :
    FontProfile(enabled, scheme) {
    val isBaseProfile: Boolean
        get() = fileTypeName.get() == AppSettings.BASE_FILE_TYPE_NAME
}