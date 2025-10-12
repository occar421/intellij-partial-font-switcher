package net.masuqat.intellij_partial_font_switcher.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "net.masuqat.intellij_partial_font_switcher.AppSettings",
    storages = [Storage("PartialFontSwitcher.xml")]
)
class AppSettings : PersistentStateComponent<AppSettings.State> {
    object State {
    }

    var appState = State

    override fun getState(): State {
        return appState
    }

    override fun loadState(value: State) {
        appState = value
    }
}