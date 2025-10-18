@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.util.NlsContexts
import com.jetbrains.rd.util.Runnable
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.SwitcherFontOptions
import javax.swing.Icon

class FileTypeFontConfigurable(
    val profile: FileTypeFontProfile,
    private val state: AppSettings.FileTypeSettingState,
    updater: Runnable
) : FontConfigurable(profile, !profile.isBaseProfile, updater) {
    private val fileTypeMap = FileTypeManager.getInstance().registeredFileTypes.associateBy { it.name }

    override fun getEditableObject(): FileTypeFontProfile = profile

    override fun getDisplayName(): @NlsContexts.ConfigurableName String =
        fileTypeMap[profile.fileTypeName]?.displayName ?: profile.fileTypeName

    override fun getIcon(expanded: Boolean): Icon? = fileTypeMap[profile.fileTypeName]?.icon

    override fun isModified(): Boolean {
        if (profile.isBaseProfile) {
            return false
        }

        return state.elementTypeSettings.base.options.fontPreferences != profile.scheme.fontPreferences
    }

    override fun apply() {} // noop
}