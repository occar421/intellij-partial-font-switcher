package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.*
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    override fun getDisplayName(): @NlsContexts.ConfigurableName String = message("plugin.name")

    class Model {
        val propertyGraph = PropertyGraph()
        val enabled = propertyGraph.property(true)
    }

    val model = Model()
    val masterDetail = FileTypeFontMasterDetail()

    private val panel = panel {
        row {
            checkBox(message("config.enable.label"))
                .bindSelected(model.enabled)
                .onIsModified { model.enabled.get() != appState.fileTypeFontState.enabled }
                .onApply { appState.fileTypeFontState.enabled = model.enabled.get() }
                .onReset { model.enabled.set(appState.fileTypeFontState.enabled) }
        }
        separator()
        row {
            cell(masterDetail.createComponent())
                .onIsModified { masterDetail.isModified }
                .onApply { masterDetail.apply() }
                .onReset { masterDetail.reset() }
        }
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
