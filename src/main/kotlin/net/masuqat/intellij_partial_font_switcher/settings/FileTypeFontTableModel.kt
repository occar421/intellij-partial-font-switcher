package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.util.ui.ListTableModel

class FileTypeFontTableModel :
    ListTableModel<FileTypeFontTableModel.FileTypeFont>(FileTypeColumnInfo(), FontColumnInfo()) {
    data class FileTypeFont(val fileType: String, val font: String) // TODO use `FileType`
}