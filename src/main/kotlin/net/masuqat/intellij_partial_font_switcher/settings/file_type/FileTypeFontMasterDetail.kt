package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.colors.FontEditorPreview
import com.intellij.application.options.editor.fonts.AppFontOptionsPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.EditorFontCache
import com.intellij.openapi.editor.colors.impl.FontPreferencesImpl
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.JBSplitter
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.util.PlatformIcons
import com.jetbrains.rd.util.Runnable
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.ListCellRenderer

class FileTypeFontMasterDetail : MasterDetailsComponent() {
    init {
        initTree()
        // TODO enlist "Default" for element type config
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        // TODO from resource
        return "File Type Font"
    }

    override fun createActions(fromPopup: Boolean): List<AnAction> {
        return listOf(
            addAction,
            // TODO delete button
        )
    }

    val addAction: AnAction
        get() {
            return object : AnAction(PlatformIcons.ADD_ICON) { /* AddAction */
                var selectedFileType = FileTypeManager.getInstance().registeredFileTypes.first()!!

                override fun actionPerformed(e: AnActionEvent) {
                    val dialog = DialogBuilder(e.project).centerPanel(panel {
                        row("FileType:") {
                            val list: Collection<FileType> = FileTypeManager.getInstance().registeredFileTypes.toList()
                            // TODO remove duplicated
                            val renderer = ListCellRenderer<FileType?> { _, value, _, _, _ ->
                                JLabel().apply {
                                    text = value?.displayName
                                    icon = value?.icon
                                }
                            }

                            comboBox(list, renderer).bindItem(::selectedFileType.toNullableProperty())
                        }
                    }).apply {
                        title("Add File Type Font") // TODO from resource
                    }

                    if (dialog.showAndGet()) {
                        addFileTypeFontNode(selectedFileType)
                    }
                }
            }
        }

    fun addFileTypeFontNode(fileType: FileType) {
        val profile = FileTypeFontProfile(fileType)
        val fileTypeFontConfigurable = FileTypeFontConfigurable(profile, TREE_UPDATER)
        val node = createFileTypeFontNode(fileTypeFontConfigurable)

        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    private fun createFileTypeFontNode(configurable: FileTypeFontConfigurable): MyNode {
        return object : MyNode(configurable) {
            override fun getLocationString(): String = "" // TODO from resource
        }
    }
}

private class FileTypeFontConfigurable(var profile: FileTypeFontProfile, updater: Runnable) :
    NamedConfigurable<FileTypeFontProfile>(false, updater) { // TODO FileType change combobox

    override fun setDisplayName(p0: @NlsSafe String?) {} // No impl.

    override fun getEditableObject(): FileTypeFontProfile = profile

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

    val scheme = createPreviewScheme() // FIXME import from another class
    val fontEditorPreview = FontEditorPreview({ scheme }, true)
    val fontOptionsPanel = AppFontOptionsPanel(scheme).apply {
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

    private fun createPreviewScheme(): EditorColorsScheme {
        val scheme = EditorColorsManager.getInstance().schemeForCurrentUITheme.clone() as EditorColorsScheme
        scheme.fontPreferences = FontPreferencesImpl()
        return scheme
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String = profile.fileType.displayName

    override fun getIcon(expanded: Boolean): Icon = profile.fileType.icon

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

class FileTypeFontProfile(val fileType: FileType)
