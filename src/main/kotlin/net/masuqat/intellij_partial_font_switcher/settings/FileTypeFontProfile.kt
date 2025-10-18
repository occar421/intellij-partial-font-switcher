package net.masuqat.intellij_partial_font_switcher.settings

import net.masuqat.intellij_partial_font_switcher.services.AppSettings

class FileTypeFontProfile(val fileTypeName: String) : FontProfile() {
    val isBaseProfile: Boolean
        get() = fileTypeName == AppSettings.BASE_FILE_TYPE_NAME
}