package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.dsl.builder.*
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.settings.configurableCell
import javax.swing.JComponent

class FileTypeConfig(propertyGraph: PropertyGraph) : UnnamedConfigurable {
    class Model(propertyGraph: PropertyGraph) {
        val enabled = propertyGraph.property(true)
    }

    val model = Model(propertyGraph)
    val table = FileTypeFontTable()

    private val panel = panel {
        row {
            checkBox("Enabled") // TODO: from resource
                .bindSelected(model.enabled)
                .onIsModified { model.enabled.get() != appState.fileTypeFontState.enabled }
                .onApply { appState.fileTypeFontState.enabled = model.enabled.get() }
                .onReset { model.enabled.set(appState.fileTypeFontState.enabled) }
        }
        row {
            configurableCell(table)
                .enabledIf(model.enabled)
                .align(AlignX.FILL)
        }.resizableRow()
    }

    override fun createComponent(): JComponent {
        return panel
    }

    override fun isModified(): Boolean {
        return panel.isModified()
    }

    override fun apply() {
        panel.apply()

        // TODO apply 後にエディタ自体に反映させる
    }

    override fun reset() {
        panel.reset()
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}