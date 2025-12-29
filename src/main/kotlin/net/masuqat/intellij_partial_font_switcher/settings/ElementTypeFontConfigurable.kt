@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.ui.DocumentAdapter
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import javax.swing.JComponent
import javax.swing.event.DocumentEvent

class ElementTypeFontConfigurable(override val profile: ElementTypeFontProfile, updateTree: Runnable) :
    FontConfigurable(true, updateTree) {
    override fun getEditableObject(): ElementTypeFontProfile = profile

    private val typeNameTextField = JBTextField(profile.elementTypeName.get()).apply {
        document.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(e: DocumentEvent) {
                profile.elementTypeName.set(text)
                updateTree.run()
            }
        })
    }

    override fun getTypeSelectorComponent(): JComponent {
        return panel {
            row(message("config.element.type.label")) {
                cell(typeNameTextField)
            }
        }
    }

    override fun isModified(): Boolean {
        return typeNameTextField.text != profile.elementTypeName.get() || super.isModified()
    }

    override fun apply() {
        profile.elementTypeName.set(typeNameTextField.text)
        super.apply()
    }
}
