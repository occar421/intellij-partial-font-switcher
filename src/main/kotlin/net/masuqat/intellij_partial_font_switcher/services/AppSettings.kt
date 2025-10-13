package net.masuqat.intellij_partial_font_switcher.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "net.masuqat.intellij_partial_font_switcher.AppSettings",
    storages = [Storage("PartialFontSwitcher.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings.State> {
    object State {
        class FileTypeFontState {
            var enabled = true
            var list = mutableListOf<FileTypeFont>()
        }

        var fileTypeFontState = FileTypeFontState()
    }

    var appState = State

    companion object {
        fun getInstance(): AppSettings? {
            return ApplicationManager.getApplication()
                .getService(AppSettings::class.java)
        }
    }

    override fun getState(): State {
        return appState
    }

    override fun loadState(value: State) {
        appState = value
    }
}

// TODO FIXME FileType
data class FileTypeFont(var fileType: String, var fontName: String)