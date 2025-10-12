package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JPanel

class AppSettingsComponent {
    val enabledCheckbox = javax.swing.JCheckBox("Enabled")

    val mainPanel = ScrollablePanel(BorderLayout()).apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(enabledCheckbox)
                    // .addSeparator()
                    .panel
            )
        }, BorderLayout.NORTH)
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
