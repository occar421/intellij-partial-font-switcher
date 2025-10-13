package net.masuqat.intellij_partial_font_switcher.file_type_level

import com.intellij.openapi.editor.Editor

val fontSelector = FileTypeFontSelector()

// TODO 適切なフォントの指定を考える
// TODO AppState （設定）を参照する

fun Editor.overrideWithFileTypeFont() {
    if (this.virtualFile != null) {
        this.colorsScheme.editorFontName = fontSelector.select(this.virtualFile.fileType)
    }
}

fun Editor.revertFileTypeFont() {
    this.colorsScheme.editorFontName = fontSelector.defaultFontName
}