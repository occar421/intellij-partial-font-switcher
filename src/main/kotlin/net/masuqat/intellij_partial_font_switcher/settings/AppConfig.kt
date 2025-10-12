package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.util.NlsContexts
import javax.swing.JComponent
import javax.swing.JPanel

class AppConfig : Configurable, Configurable.Beta {
    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "Partial Font Switcher" // TODO: from resource
    }

    override fun createComponent(): JComponent {
        return JPanel() // TODO: impl
    }

    override fun isModified(): Boolean {
        return false // TODO: impl
    }

    override fun apply() {
        // TODO: impl
    }
}