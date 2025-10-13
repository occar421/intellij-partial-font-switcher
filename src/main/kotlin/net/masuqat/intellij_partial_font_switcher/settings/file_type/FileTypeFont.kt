package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.FileTypeFont
import javax.swing.JComponent

class FileTypeFontTable : UnnamedConfigurable {
    class Model :
        ListTableModel<Model.FileTypeFont>(FileTypeColumnInfo(), FontColumnInfo()) {
        data class FileTypeFont(val fileType: String, val font: String) // FIXME use `FileType`
    }

    val model = Model()

    val table = JBTable(model).apply {
        emptyText.text = "No FileType font settings" // TODO from resource
        visibleRowCount = 5
    }

    override fun createComponent(): JComponent {
        return ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(Model.FileTypeFont("Aiueo", "Impact2"))
            }
            .setRemoveAction {
                model.removeRow(table.selectedRow)
            }
            .createPanel()
    }

    override fun isModified(): Boolean {
        if (appState.fileTypeFontState.list.size != model.items.size) return true
        return appState.fileTypeFontState.list.zip(model.items).any { (setting, model) ->
            setting.fileType != model.fileType || setting.fontName != model.font
        }
    }

    override fun apply() {
        appState.fileTypeFontState.list.clear()
        appState.fileTypeFontState.list.addAll(model.items.map { m -> FileTypeFont(m.fileType, m.font) })
    }

    override fun reset() {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(appState.fileTypeFontState.list.map { s -> Model.FileTypeFont(s.fileType, s.fontName) })
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}

class FileTypeColumnInfo : ColumnInfo<FileTypeFontTable.Model.FileTypeFont, String>("FileType") { // TODO from resource
    override fun valueOf(lf: FileTypeFontTable.Model.FileTypeFont?): String? = lf?.fileType
}

class FontColumnInfo : ColumnInfo<FileTypeFontTable.Model.FileTypeFont, String>("Font") { // TODO from resource
    override fun valueOf(lf: FileTypeFontTable.Model.FileTypeFont?): String? = lf?.font
}