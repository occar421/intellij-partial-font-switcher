package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.util.PlatformIcons
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import net.masuqat.intellij_partial_font_switcher.services.SwitcherFontOptions
import javax.swing.JLabel
import javax.swing.ListCellRenderer

class FileTypeFontMasterDetail(private val state: AppSettings.FileTypeSettingsState) : MasterDetailsComponent() {
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
        val profile = FileTypeFontProfile(fileType.name, FontProfile.createInitialScheme())
        val configurable =
            FileTypeFontConfigurable(profile, AppSettings.FileTypeSettingState(fileType.name), TREE_UPDATER)
        val node = FileTypeFontNode(configurable)

        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    abstract class FontNode(configurable: FontConfigurable) : MyNode(configurable) {
        override fun getLocationString(): String = message("config.setting.location.label")
    }

    class FileTypeFontNode(val configurable: FileTypeFontConfigurable) : FontNode(configurable)

    override fun isModified(): Boolean {
        return myRoot.children().asSequence().map { it as FontNode }.any { it.configurable.isModified }
    }

    override fun apply() {
        val fileTypeProfiles = myRoot.children().asSequence().map { it as FileTypeFontNode }.map { it.configurable }
        val groups = fileTypeProfiles.groupBy { it.profile.isBaseProfile }
//        groups[true]?.forEach { appState.fileTypeSettings.base }
        state.additional = groups[false]?.map {
            AppSettings.FileTypeSettingState(it.profile.fileTypeName, AppSettings.ElementTypeSettingsState().apply {
                base = AppSettings.ElementTypeSettingState(
                    AppSettings.BASE_ELEMENT_TYPE_NAME, SwitcherFontOptions().apply {
                        update(it.profile.scheme.fontPreferences)
                    })
            })
        }?.toCollection(mutableListOf()) ?: mutableListOf()
    }

    override fun reset() {
        myRoot.removeAllChildren()

        val profile = FileTypeFontProfile(state.base.fileTypeName, FontProfile.createInitialScheme()) // Do not insert preference to follow global font
        val baseNode = FileTypeFontNode(FileTypeFontConfigurable(profile, state.base, TREE_UPDATER))
        myRoot.add(baseNode)

        state.additional.forEach {
            val profile = FileTypeFontProfile(
                it.fileTypeName, FontProfile.createInitialScheme().apply {
                    fontPreferences = it.elementTypeSettings.base.options.fontPreferences
                })
            val baseNode = FileTypeFontNode(FileTypeFontConfigurable(profile, it, TREE_UPDATER))
            myRoot.add(baseNode)
        }

        super.reset()
    }
}
