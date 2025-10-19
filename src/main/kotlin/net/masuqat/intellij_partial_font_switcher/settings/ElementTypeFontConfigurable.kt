@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.tree.IElementType
import com.jetbrains.rd.util.Runnable
import javax.swing.JComponent

class ElementTypeFontConfigurable(override val profile: ElementTypeFontProfile, updater: Runnable) :
    FontConfigurable(true, updater) {
    val elementTypeMap = IElementType.enumerate { true }.associateBy { it.toString() }

    override fun getEditableObject(): ElementTypeFontProfile = profile

    override fun getBannerSlogan(): @NlsContexts.DetailedDescription String? = null

    override fun getDisplayName(): @NlsContexts.ConfigurableName String =
        elementTypeMap[profile.elementTypeName]?.debugName ?: profile.elementTypeName

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