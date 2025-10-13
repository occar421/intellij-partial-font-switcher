package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.*
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "Partial Font Switcher" // TODO: from resource
    }

    class Model {
        private val propertyGraph = PropertyGraph()

        var enabled = propertyGraph.property(true)
        var languageFonts = LanguageFontTable()
    }

    val panel = panel {
        val model = Model()

        row {
            checkBox("Enabled") // TODO: from resource
                .bindSelected(model.enabled)
                .onIsModified { model.enabled.get() != appState.enabled }
                .onApply { appState.enabled = model.enabled.get() }
                .onReset { model.enabled.set(appState.enabled) }
        }
        row {
            cell(model.languageFonts.createComponent())
                .align(Align.FILL)
                .enabledIf(model.enabled)
                .onIsModified { model.languageFonts.isModified }
                .onApply { model.languageFonts.apply() }
                .onReset { model.languageFonts.reset() }
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
