package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.NlsContexts
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    val appSettingsComponent = AppSettingsComponent()
    val configModel: AppSettingsComponent.Model
        get() = appSettingsComponent.model

    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "Partial Font Switcher" // TODO: from resource
    }

    override fun createComponent(): JComponent {
        return appSettingsComponent.mainPanel
    }

    override fun isModified(): Boolean {
        return appState.enabled != configModel.enabled
    }

    override fun apply() {
        appState.enabled = configModel.enabled

        // TODO apply 後に反映させる
    }

    override fun reset() {
        configModel.enabled = appState.enabled
    }

    private val appState: AppSettings.State
        get() = AppSettings.getInstance()!!.appState
}