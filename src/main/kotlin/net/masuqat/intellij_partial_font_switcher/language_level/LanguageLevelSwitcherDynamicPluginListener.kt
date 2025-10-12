package net.masuqat.intellij_partial_font_switcher.language_level

import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.openapi.editor.EditorFactory

class LanguageLevelSwitcherDynamicPluginListener : DynamicPluginListener {
    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        EditorFactory.getInstance().allEditors.forEach {
            it.overrideWithLanguageFont()
        }
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        EditorFactory.getInstance().allEditors.forEach {
            it.revertLanguageFont()
        }
    }
}