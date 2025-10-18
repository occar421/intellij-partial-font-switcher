@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.tree.IElementType
import com.jetbrains.rd.util.Runnable
import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class ElementTypeFontConfigurable(
    val profile: ElementTypeFontProfile,
    private val elementTypeSettingState: AppSettings.ElementTypeSettingState,
    updater: Runnable
) : FontConfigurable(profile, true, updater) {
    val elementTypeMap = IElementType.enumerate { true }.associateBy { it.toString() }

    override fun getEditableObject(): ElementTypeFontProfile = profile

    override fun getBannerSlogan(): @NlsContexts.DetailedDescription String? = null

    override fun getDisplayName(): @NlsContexts.ConfigurableName String =
        elementTypeMap[profile.elementTypeName]?.debugName ?: profile.elementTypeName

    override fun isModified(): Boolean {
//        TODO("Not yet implemented")
        return false
    }

    override fun apply() {
//        TODO("Not yet implemented")
    }
}