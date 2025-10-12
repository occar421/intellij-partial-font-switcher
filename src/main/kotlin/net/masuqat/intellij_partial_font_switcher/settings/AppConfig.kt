package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.NlsContexts
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    val appSettingsComponent = AppSettingsComponent()

    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "Partial Font Switcher" // TODO: from resource
    }

    override fun createComponent(): JComponent {
        return appSettingsComponent.mainPanel
    }

    override fun isModified(): Boolean {
        val state = appState
        val model = appSettingsComponent.settingModel

        return state.enabled != model.enabled
    }

    override fun apply() {
        val state = appState
        val model = appSettingsComponent.settingModel

        state.enabled = model.enabled

        // TODO apply 後に反映させる
    }

    override fun reset() {
        val state = appState
        val model = appSettingsComponent.settingModel

        model.enabled = state.enabled
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}