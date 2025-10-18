package net.masuqat.intellij_partial_font_switcher.file_type_level

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.editor.EditorFactory

class FileTypeLevelSwitcherDynamicPluginListener : DynamicPluginListener {
    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        EditorFactory.getInstance().allEditors.forEach {
            it.switchFontPreference()
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        EditorFactory.getInstance().allEditors.forEach {
            it.revertFontPreference()
        }
    }
}