package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.roots.ui.componentsList.components.ScrollablePanel
import com.intellij.ui.FontComboBox
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class AppSettingsComponent {
    val enabledCheckbox = JBCheckBox("Enabled").apply {
        addActionListener {
            settingModel.enabled = isSelected
        }
    }
    val fontLabel = JLabel("Font:")
    val fontComboBox = FontComboBox().apply {
        addActionListener {
            settingModel.fontName = fontName
        }
    }

    val mainPanel = ScrollablePanel(BorderLayout()).apply {
        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.PAGE_AXIS)

            add(
                FormBuilder.createFormBuilder()
                    .addComponent(enabledCheckbox)
                    .addSeparator()
                    .addLabeledComponent(fontLabel, fontComboBox)
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
            set(value) {
                component.enabledCheckbox.isSelected = value

                component.fontLabel.isEnabled = value
                component.fontComboBox.isEnabled = value
            }

        var fontName: String?
            get() = component.fontComboBox.fontName
            set(value) {
                component.fontComboBox.fontName = value
            }
    }
}
