package net.masuqat.intellij_partial_font_switcher.language_level

import com.intellij.openapi.editor.Editor

val fontSelector = LanguageFontSelector()

// TODO 適切なフォントの指定を考える

fun Editor.overrideWithLanguageFont() {
    this.colorsScheme.editorFontName = fontSelector.select(this.virtualFile.fileType)
}

fun Editor.revertLanguageFont() {
    this.colorsScheme.editorFontName = fontSelector.defaultFontName
}