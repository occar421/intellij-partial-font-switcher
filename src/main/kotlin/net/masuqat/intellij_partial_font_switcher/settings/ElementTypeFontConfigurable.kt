@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.jetbrains.rd.util.Runnable
import javax.swing.JComponent

class ElementTypeFontConfigurable(override val profile: ElementTypeFontProfile, updater: Runnable) :
    FontConfigurable(true, updater) {
    override fun getEditableObject(): ElementTypeFontProfile = profile

    override fun getTypeSelectorComponent(): JComponent {
        TODO("Not yet implemented")
    }

    override fun isModified(): Boolean {
        TODO("Not yet implemented")
    }

    override fun apply() {
        TODO("Not yet implemented")
    }
}