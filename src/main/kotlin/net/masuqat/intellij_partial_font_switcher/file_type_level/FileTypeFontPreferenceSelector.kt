package net.masuqat.intellij_partial_font_switcher.file_type_level

import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.fileTypes.FileType
import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class FileTypeFontPreferenceSelector {
    fun select(fileType: FileType): FontPreferences {
        if (!appState.enabled) {
            return globalFontPreference
        }

        val fileTypeSetting = appState.fileTypeSettings.additional.firstOrNull { it.fileTypeName == fileType.name }
        if (!(fileTypeSetting?.enabled ?: false)) {
            return globalFontPreference
        }

        return fileTypeSetting.elementTypeSettings.base.options.fontPreferences
    }

    val globalFontPreference: FontPreferences
        get() = EditorColorsManager.getInstance().globalScheme.fontPreferences

    private val appState: AppSettings.RootState
        get() = AppSettings.getInstance()!!.appState
}