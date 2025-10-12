package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel
import javax.swing.JPanel

class AppSettingsComponent {
    val enabledCheckbox = javax.swing.JCheckBox("Enabled")

    val mainPanel = ScrollablePanel().apply {
        layout = java.awt.BorderLayout()
        add(JPanel().apply {
            add(enabledCheckbox)
        }, java.awt.BorderLayout.WEST)
    }

    val settingModel: SettingModel
        get() {
            return SettingModel(this)
        }

    class SettingModel(val component: AppSettingsComponent) {
        var enabled: Boolean
            get() = component.enabledCheckbox.isSelected
            set(value) = component.enabledCheckbox.setSelected(value)
    }
}
