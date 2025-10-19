package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.observable.properties.GraphProperty

class ElementTypeFontProfile(val elementTypeName: String, enabled: GraphProperty<Boolean>, scheme: EditorColorsScheme) :
    FontProfile(enabled, scheme)