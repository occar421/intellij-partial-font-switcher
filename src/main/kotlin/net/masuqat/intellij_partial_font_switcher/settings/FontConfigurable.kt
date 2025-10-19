@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.colors.FontEditorPreview
import com.intellij.application.options.editor.fonts.AppFontOptionsPanel
import com.intellij.openapi.editor.colors.EditorFontCache
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.JBSplitter
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

abstract class FontConfigurable(profile: FontProfile, private val editable: Boolean, updater: Runnable) :
    NamedConfigurable<FontProfile>(false, updater) {
    val scheme = profile.scheme

    override fun setDisplayName(p0: @NlsSafe String?) {} // No impl.

    override fun getBannerSlogan(): @NlsContexts.DetailedDescription String? = null

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                cell(JBSplitter(false, 0.3f).apply {
                    firstComponent = fontOptionsPanel
                    secondComponent = fontEditorPreview.panel
                })
            }
        }
    }

    protected val fontEditorPreview = FontEditorPreview({ scheme }, editable)
    protected val fontOptionsPanel = object : AppFontOptionsPanel(scheme) {
        override fun isReadOnly(): Boolean = !editable
        override fun isEnabled(): Boolean = !editable
    }.apply {
        addListener(object : ColorAndFontSettingsListener.Abstract() {
            override fun fontChanged() {
                updatePreview()
            }
        })
    }

    private fun updatePreview() {
        if (scheme is EditorFontCache) {
            (scheme as EditorFontCache).reset()
        }
        fontEditorPreview.updateView()
    }
}
