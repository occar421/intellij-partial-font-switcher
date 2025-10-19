package net.masuqat.intellij_partial_font_switcher.editor_mod

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class FileTypeLevelSwitcherEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        event.editor.switchFontPreference()
    }
}