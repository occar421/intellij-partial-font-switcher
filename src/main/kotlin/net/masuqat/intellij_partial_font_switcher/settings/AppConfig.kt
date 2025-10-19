package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorFontCache
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.file_type_level.switchFontPreference
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    override fun getDisplayName(): @NlsContexts.ConfigurableName String = message("plugin.name")

    class Model {
        val propertyGraph = PropertyGraph()
        val enabled = propertyGraph.property(true)
    }

    val model = Model()
    val masterDetail = SwitcherMasterDetail(appState.fileTypeSettings, model.propertyGraph)

    private val panel = panel {
        row {
            checkBox(message("config.enable.label"))
                .bindSelected(model.enabled)
                .onIsModified { model.enabled.get() != appState.enabled }
                .onApply { appState.enabled = model.enabled.get() }
                .onReset { model.enabled.set(appState.enabled) }
        }
        separator()
        row {
            cell(masterDetail.createComponent())
                .onIsModified { masterDetail.isModified }
                .onApply { masterDetail.apply() }
                .onReset { masterDetail.reset() }
                .align(AlignX.FILL)
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

        EditorFontCache.getInstance().reset()
        EditorFactory.getInstance().allEditors.forEach {
            it.switchFontPreference()
        }
        EditorFactory.getInstance().refreshAllEditors()
    }

    override fun reset() {
        panel.reset()
    }

    private val appState: AppSettings.RootState
        get() = AppSettings.getInstance()!!.appState
}
