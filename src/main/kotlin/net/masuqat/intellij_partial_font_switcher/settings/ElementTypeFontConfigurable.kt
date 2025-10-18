@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.colors.FontEditorPreview
import com.intellij.application.options.editor.fonts.AppFontOptionsPanel
import com.intellij.openapi.editor.colors.EditorFontCache
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.psi.tree.IElementType
import com.intellij.ui.JBSplitter
import com.intellij.ui.dsl.builder.panel
import com.jetbrains.rd.util.Runnable
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.JComponent

class ElementTypeFontConfigurable(
    val profile: ElementTypeFontProfile,
    private val elementTypeSettingState: AppSettings.ElementTypeSettingState,
    updater: Runnable
) : NamedConfigurable<ElementTypeFontProfile>(false, updater) {
    val elementTypeMap = IElementType.enumerate { true }.associateBy { it.toString() }

    override fun setDisplayName(p0: @NlsSafe String?) {} // No impl.

    override fun getEditableObject(): ElementTypeFontProfile = profile

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

    val fontEditorPreview = FontEditorPreview({ profile.scheme }, true)
    val fontOptionsPanel = AppFontOptionsPanel(profile.scheme).apply {
        addListener(object : ColorAndFontSettingsListener.Abstract() {
            override fun fontChanged() {
                updatePreview()
            }
        })
    }

    private fun updatePreview() {
        if (profile.scheme is EditorFontCache) {
            (profile.scheme as EditorFontCache).reset()
        }
        fontEditorPreview.updateView()
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String =
        elementTypeMap[profile.elementTypeName]?.debugName ?: profile.elementTypeName

    override fun isModified(): Boolean {
//        TODO("Not yet implemented")
        return false
    }

    override fun apply() {
//        TODO("Not yet implemented")
    }

    override fun disposeUIResources() {
        fontEditorPreview.disposeUIResources()
    }
}