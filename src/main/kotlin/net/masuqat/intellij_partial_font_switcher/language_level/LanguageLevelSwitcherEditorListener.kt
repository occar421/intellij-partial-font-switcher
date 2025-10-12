package net.masuqat.intellij_partial_font_switcher.language_level

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class LanguageLevelSwitcherEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        event.editor.overrideWithLanguageFont()
    }
}