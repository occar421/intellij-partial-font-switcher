@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.colors.FontEditorPreview
import com.intellij.application.options.editor.fonts.AppFontOptionsPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.colors.EditorFontCache
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
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.ListCellRenderer

class FileTypeFontMasterDetail(private val fileTypeSettingsState: AppSettings.FileTypeSettingsState) :
    MasterDetailsComponent() {
    init {
        initTree()

        // TODO tree view for element type config
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String = message("config.setting.title")

    override fun createActions(fromPopup: Boolean): List<AnAction> {
        return listOf(
            createAddAction(),
            // TODO delete button
        )
    }

    private fun createAddAction(): AnAction {
        return object : AnAction(PlatformIcons.ADD_ICON) { /* AddAction */
            var selectedFileType = FileTypeManager.getInstance().registeredFileTypes.first()!!

            override fun actionPerformed(e: AnActionEvent) {
                val dialog = DialogBuilder(e.project).centerPanel(panel {
                    row(message("config.action.add_file_type_setting.label")) {
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
                    title(message("config.action.add_file_type_setting.title"))
                }

                if (dialog.showAndGet()) {
                    addNewFileTypeNode(selectedFileType)
                }
            }
        }
    }

    private fun addNewFileTypeNode(fileType: FileType) {
        val profile = FileTypeFontProfile(fileType.name)
        val fileTypeFontConfigurable =
            FileTypeFontConfigurable(profile, AppSettings.FileTypeSettingState(fileType.name), TREE_UPDATER)
        val node = FileTypeFontNode(fileTypeFontConfigurable)

        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    class FileTypeFontNode(val configurable: FileTypeFontConfigurable) : MyNode(configurable) {
        override fun getLocationString(): String = message("config.setting.location.label")
    }

    override fun isModified(): Boolean {
        return myRoot.children().asSequence().map { it as FileTypeFontNode }.any { it.configurable.isModified }
    }

    override fun apply() {
        myRoot.children().asSequence().map { it as FileTypeFontNode }.forEach { it.configurable.apply() }
    }

    override fun reset() {
        myRoot.removeAllChildren()

        resetFileTypeNode(fileTypeSettingsState.base)
        fileTypeSettingsState.additional.forEach { resetFileTypeNode(it) }

        super.reset()
    }

    private fun resetFileTypeNode(settingState: AppSettings.FileTypeSettingState) {
        val profile = FileTypeFontProfile(settingState.fileTypeName);
        val baseNode = FileTypeFontNode(FileTypeFontConfigurable(profile, settingState, TREE_UPDATER))
        myRoot.add(baseNode)
    }
}

class FileTypeFontConfigurable(
    val profile: FileTypeFontProfile,
    private val fileTypeSettingState: AppSettings.FileTypeSettingState,
    updater: Runnable
) : NamedConfigurable<FileTypeFontProfile>(false, updater) { // TODO FileType change combobox
    val fileTypeMap = FileTypeManager.getInstance().registeredFileTypes.associateBy { it.name }

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
        fileTypeMap[profile.fileTypeName]?.displayName ?: profile.fileTypeName

    override fun getIcon(expanded: Boolean): Icon? = fileTypeMap[profile.fileTypeName]?.icon

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

class FileTypeFontProfile(val fileTypeName: String) {
    val scheme = createScheme()

    private fun createScheme(): EditorColorsScheme {
        val globalScheme = EditorColorsManager.getInstance().globalScheme
        val scheme = globalScheme.clone() as EditorColorsScheme
        scheme.fontPreferences = globalScheme.fontPreferences // to be editable
        return scheme
    }
}