package net.masuqat.intellij_partial_font_switcher

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class AppSettingsTest : BasePlatformTestCase() {

    fun testSettingsPersistence() {
        val settings = AppSettings.getInstance()
        assertNotNull("AppSettings service should be available", settings)

        val initialState = settings!!.appState.enabled
        try {
            settings.appState.enabled = !initialState
            assertEquals("Settings state should be updated", !initialState, AppSettings.getInstance()!!.appState.enabled)
        } finally {
            // テスト後に状態を復元
            settings.appState.enabled = initialState
        }
    }
}
