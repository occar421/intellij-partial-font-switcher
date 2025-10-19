@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.Runnable
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class FileTypeFontConfigurable(
    override val profile: FileTypeFontProfile,
    private val state: AppSettings.FileTypeSettingState,
    updateTree: Runnable
) : FontConfigurable(!profile.isBaseProfile, updateTree) {
    private val fileTypeMap = FileTypeManager.getInstance().registeredFileTypes.associateBy { it.name }

    override fun getEditableObject(): FileTypeFontProfile = profile

    override fun getTypeSelectorComponent(): JComponent {
        if (profile.isBaseProfile) {
            return panel {
                row(message("config.setting.file_type.label")) {
                    cell(ComboBox(emptyArray<String>())).enabled(false)
                }
            }
        }

        val allFileNames = fileTypeMap.keys.toSet()
        val existingFileNames = profile.existingFileNames.getter.call().toSet()
        val availableFileTypes = allFileNames - existingFileNames + setOf(profile.fileTypeName.get())

        val comboBox = ComboBox(availableFileTypes.sortedBy { fileTypeMap[it]?.displayName }.toTypedArray()).apply {
            renderer = SimpleListCellRenderer.create { label, value, _ ->
                label.text = fileTypeMap[value]?.displayName ?: value
                label.icon = fileTypeMap[value]?.icon
            }
            isSwingPopup = false
        }

        return panel {
            row(message("config.setting.file_type.label")) {
                cell(comboBox).bindItem(profile.fileTypeName)
            }
        }
    }

    override fun isModified(): Boolean {
        if (profile.isBaseProfile) {
            return false
        }

        return state.fileTypeName != profile.fileTypeName.get()
                || state.enabled != profile.enabled.get()
                || state.elementTypeSettings.base.options.fontPreferences != profile.scheme.fontPreferences
    }

    override fun apply() {} // noop
}