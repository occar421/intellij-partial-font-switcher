package net.masuqat.intellij_partial_font_switcher.settings

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
        appState.languageFonts.clear()
        appState.languageFonts.addAll(model.items.map { m -> LanguageFontSetting(m.language, m.font) })
    }

    override fun reset() {
        repeat(model.items.size) { model.removeRow(0) }
        model.addRows(appState.languageFonts.map { s -> LanguageFontTableModel.LanguageFont(s.language, s.fontName) })
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}

class FileTypeColumnInfo : ColumnInfo<LanguageFontTableModel.LanguageFont, String>("FileType") { // TODO from resource
    override fun valueOf(lf: LanguageFontTableModel.LanguageFont?): String? = lf?.language
}

class FontColumnInfo : ColumnInfo<LanguageFontTableModel.LanguageFont, String>("FontColumn") { // TODO from resource
    override fun valueOf(lf: LanguageFontTableModel.LanguageFont?): String? = lf?.font
}