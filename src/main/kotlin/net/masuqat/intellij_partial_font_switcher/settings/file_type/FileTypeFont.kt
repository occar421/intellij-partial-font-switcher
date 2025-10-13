package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.openapi.ui.ComboBoxTableRenderer
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.TableView
import com.intellij.util.ui.ColumnInfo
import com.intellij.util.ui.ListTableModel
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.FileTypeFont
import javax.swing.Icon
import javax.swing.JComponent

class FileTypeFontTable : UnnamedConfigurable {
    class Model :
        ListTableModel<Model.FileTypeFont>(FileTypeColumnInfo(), FontColumnInfo()) {
        data class FileTypeFont(var fileType: FileType, var font: String)
    }

    val model = Model()

    val table = TableView(model).apply {
        emptyText.text = "No FileType font settings" // TODO from resource
        visibleRowCount = 5
    }

    override fun createComponent(): JComponent {
        return ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(Model.FileTypeFont(fileTypes().first(), "Impact2"))
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
    private val fileTypeMap = fileTypes().associateBy { type -> type.name }

    override fun valueOf(lf: FileTypeFontTable.Model.FileTypeFont?): String? = lf?.fileType?.name

    override fun setValue(item: FileTypeFontTable.Model.FileTypeFont?, value: String?) {
        item?.fileType = fileTypeMap[value] ?: return
    }

    override fun getEditor(item: FileTypeFontTable.Model.FileTypeFont?) = createComboBoxRendererAndEditor()

    override fun getRenderer(item: FileTypeFontTable.Model.FileTypeFont?) = createComboBoxRendererAndEditor()

    override fun isCellEditable(item: FileTypeFontTable.Model.FileTypeFont?) = true

    private fun createComboBoxRendererAndEditor(): ComboBoxTableRenderer<String> {
        val fileTypes = fileTypes().map { it.name }

        return FileTypeCellComboBox(fileTypes.toTypedArray()).withClickCount(1)
    }

    private class FileTypeCellComboBox(values: Array<String>) : ComboBoxTableRenderer<String>(values) {
        private val fileTypeMap = fileTypes().associateBy { type -> type.name }

        override fun getTextFor(value: String): @NlsContexts.Label String =
            fileTypeMap[value]?.displayName ?: "???" // TODO from resource

        override fun getIconFor(value: String): Icon? = fileTypeMap[value]?.icon
    }
}

class FontColumnInfo : ColumnInfo<FileTypeFontTable.Model.FileTypeFont, String>("Font") { // TODO from resource
    override fun valueOf(lf: FileTypeFontTable.Model.FileTypeFont?): String? = lf?.font
}

private fun fileTypes() = FileTypeManager.getInstance().registeredFileTypes