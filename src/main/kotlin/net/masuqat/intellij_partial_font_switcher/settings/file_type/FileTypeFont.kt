package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.FileTypeFontSetting
import javax.swing.JComponent

class FileTypeFontTable : UnnamedConfigurable {
    val model = FileTypeFontTableModel()

    val table = JBTable(model).apply {
        emptyText.text = "No FileType font settings" // TODO from resource
        visibleRowCount = 5
    }

    override fun createComponent(): JComponent {
        return ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(FileTypeFontTableModel.FileTypeFont("Aiueo", "Impact2"))
            }
            .setRemoveAction {
                model.removeRow(table.selectedRow)
            }
            .createPanel()
    }

    override fun isModified(): Boolean {
        if (appState.fileTypeFonts.size != model.items.size) return true
        return appState.fileTypeFonts.zip(model.items).any { (setting, model) ->
            setting.fileType != model.fileType || setting.fontName != model.font
        }
    }

    override fun apply() {
        appState.fileTypeFonts.clear()
        appState.fileTypeFonts.addAll(model.items.map { m -> FileTypeFontSetting(m.fileType, m.font) })
    }

    override fun reset() {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(appState.fileTypeFonts.map { s -> FileTypeFontTableModel.FileTypeFont(s.fileType, s.fontName) })
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}

class FileTypeColumnInfo : ColumnInfo<FileTypeFontTableModel.FileTypeFont, String>("FileType") { // TODO from resource
    override fun valueOf(lf: FileTypeFontTableModel.FileTypeFont?): String? = lf?.fileType
}

class FontColumnInfo : ColumnInfo<FileTypeFontTableModel.FileTypeFont, String>("Font") { // TODO from resource
    override fun valueOf(lf: FileTypeFontTableModel.FileTypeFont?): String? = lf?.font
}