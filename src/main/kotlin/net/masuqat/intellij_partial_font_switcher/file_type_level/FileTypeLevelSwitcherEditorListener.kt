package net.masuqat.intellij_partial_font_switcher.file_type_level

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class FileTypeLevelSwitcherEditorListener : EditorFactoryListener {
    override fun editorCreated(event: EditorFactoryEvent) {
        event.editor.overrideWithFileTypeFont()
    }
}