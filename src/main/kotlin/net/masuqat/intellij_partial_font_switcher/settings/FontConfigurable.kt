@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.editor.fonts.AppFontOptionsPanel
import com.intellij.openapi.editor.colors.EditorFontCache
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.JBSplitter
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.settings.cloned_components.FontEditorPreview
import javax.swing.Icon
import javax.swing.JComponent

abstract class FontConfigurable(private val editable: Boolean, val updateTree: Runnable) :
    NamedConfigurable<FontProfile>(false, updateTree) {
    abstract val profile: FontProfile

    final override fun setDisplayName(p0: @NlsSafe String?) {} // noop

    final override fun getDisplayName(): @NlsContexts.ConfigurableName String? = null // noop

    final override fun getIcon(expanded: Boolean): Icon? = null

    final override fun getBannerSlogan(): @NlsContexts.DetailedDescription String? = null // noop

    abstract fun getTypeSelectorComponent(): JComponent

    override fun createOptionsPanel(): JComponent {
        val fontEditorPreview = FontEditorPreview({ profile.scheme }, editable)
        val fontOptionsPanel = object : AppFontOptionsPanel(profile.scheme) {
            override fun isReadOnly(): Boolean = !editable || !profile.enabled.get()
            override fun isEnabled(): Boolean = editable && profile.enabled.get()
        }.apply {
            addListener(object : ColorAndFontSettingsListener.Abstract() {
                override fun fontChanged() {
                    if (profile.scheme is EditorFontCache) {
                        (profile.scheme as EditorFontCache).reset()
                    }
                    fontEditorPreview.updateView()
                }
            })

            profile.enabled.afterChange {
                updateOptionsList()
            }
        }

        return panel {
            row {
                cell(getTypeSelectorComponent())
            }
            row {
                checkBox(message("config.setting.enable.label"))
                    .bindSelected(profile.enabled)
                    .enabled(editable)
            }
            separator()
            row {
                cell(JBSplitter(false, 0.3f).apply {
                    firstComponent = fontOptionsPanel
                    secondComponent = fontEditorPreview.panel
                }).align(AlignX.FILL)
            }
        }
    }
}
