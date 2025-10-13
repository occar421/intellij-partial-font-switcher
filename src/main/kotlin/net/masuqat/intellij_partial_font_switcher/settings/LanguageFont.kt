package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.LanguageFontSetting
import javax.swing.JComponent

class LanguageFontTable : UnnamedConfigurable {
    val model = LanguageFontTableModel()

    val table = JBTable(model).apply {
        emptyText.text = "No language fonts" // TODO from resource
    }

    override fun createComponent(): JComponent {
        return ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.addRow(LanguageFontTableModel.LanguageFont("Aiueo", "Impact2"))
            }
            .setRemoveAction {
                model.removeRow(table.selectedRow)
            }
            .createPanel()
    }

    override fun isModified(): Boolean {
        if (appState.languageFonts.size != model.items.size) return true
        return appState.languageFonts.zip(model.items).any { (setting, model) ->
            setting.language != model.language || setting.fontName != model.font
        }
    }

    override fun apply() {
        this.appState.languageFonts.clear()
        this.appState.languageFonts.addAll(model.items.map { m -> LanguageFontSetting(m.language, m.font) })
    }

    override fun reset() {
        this.model.items.clear()
        this.model.items.addAll(this.appState.languageFonts.map { s ->
            LanguageFontTableModel.LanguageFont(
                s.language,
                s.fontName
            )
        })

        this.model.fireTableDataChanged()
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}

class FileTypeColumnInfo : ColumnInfo<FileType, String>("FileType") { // TODO from resource
    override fun valueOf(p0: FileType?): String {
//        TODO("Not yet implemented")
        return "TestFileType"
    }
}

class FontColumnInfo : ColumnInfo<String, String>("FontColumn") { // TODO from resource
    override fun valueOf(p0: String?): String {
//        TODO("Not yet implemented")
        return "TestFont"
    }

}