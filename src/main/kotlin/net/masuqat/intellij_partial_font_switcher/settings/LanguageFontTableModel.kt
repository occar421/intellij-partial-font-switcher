package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.util.ui.ListTableModel

class LanguageFontTableModel :
    ListTableModel<LanguageFontTableModel.LanguageFont>(FileTypeColumnInfo(), FontColumnInfo()) {
    data class LanguageFont(val language: String, val font: String)
}