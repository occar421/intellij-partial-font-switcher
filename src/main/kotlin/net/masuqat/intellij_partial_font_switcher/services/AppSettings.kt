package net.masuqat.intellij_partial_font_switcher.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.SettingsCategory
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.colors.impl.AppEditorFontOptions
import com.intellij.openapi.editor.colors.impl.AppFontOptions

@State(
    name = "net.masuqat.intellij_partial_font_switcher.AppSettings",
    storages = [Storage("PartialFontSwitcher.xml")],
    category = SettingsCategory.UI
)
class AppSettings : PersistentStateComponent<AppSettings.RootState> {
    object RootState {
        var enabled = true
        var fileTypeSettings = FileTypeSettingsState()
    }

    var appState = RootState

    companion object {
        fun getInstance(): AppSettings? = ApplicationManager.getApplication().getService(AppSettings::class.java)

        const val BASE_FILE_TYPE_NAME = "Base"
        const val BASE_ELEMENT_TYPE_NAME = "Base"
    }

    override fun getState(): RootState {
        return appState
    }

    override fun loadState(value: RootState) {
        appState = value
    }

    class FileTypeSettingsState {
        var base = FileTypeSettingState(BASE_FILE_TYPE_NAME)
        var additional = mutableListOf<FileTypeSettingState>()
    }

    class FileTypeSettingState(
        var fileTypeName: String,
        var elementTypeSettings: ElementTypeSettingsState = ElementTypeSettingsState()
    )

    class ElementTypeSettingsState {
        var base = ElementTypeSettingState(BASE_ELEMENT_TYPE_NAME)
        var additional = mutableListOf<ElementTypeSettingState>()
    }

    class ElementTypeSettingState(
        var elementTypeName: String,
        var options: SwitcherFontOptions = SwitcherFontOptions()
    )

    class SwitcherFontOptions : AppFontOptions<AppEditorFontOptions.PersistentFontPreferences>() {
        override fun createFontState(p0: FontPreferences): AppEditorFontOptions.PersistentFontPreferences =
            AppEditorFontOptions.PersistentFontPreferences(fontPreferences)
    }
}
