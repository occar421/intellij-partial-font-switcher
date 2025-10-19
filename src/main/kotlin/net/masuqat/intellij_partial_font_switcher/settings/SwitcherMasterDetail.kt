package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.openapi.util.NlsContexts
import com.intellij.util.PlatformIcons
import net.masuqat.intellij_partial_font_switcher.Bundle.message
import net.masuqat.intellij_partial_font_switcher.services.AppSettings
import java.util.Comparator

class SwitcherMasterDetail(
    private val state: AppSettings.FileTypeSettingsState, private val propertyGraph: PropertyGraph
) : MasterDetailsComponent() {
    init {
        initTree()

        // TODO tree view for element type config
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String = message("config.setting.title")

    val allFileTypeSwitcherNodes: Sequence<FileTypeSwitcherNode>
        get() = myRoot.children().asSequence().filterIsInstance<FileTypeSwitcherNode>()
    val allFileTypeConfigurables: Sequence<FileTypeFontConfigurable>
        get() = allFileTypeSwitcherNodes.map { it.configurable }
    val existingFileNames: Sequence<String>
        get() = allFileTypeConfigurables.map { it.profile.fileTypeName.get() }


    override fun createActions(fromPopup: Boolean): List<AnAction> {
        return listOf(
            createAddAction(),
            createDeleteAction(),
        )
    }

    private fun createAddAction(): AnAction {
        val fileTypeMap = FileTypeManager.getInstance().registeredFileTypes.associateBy { it.name }
        val allFileNames = fileTypeMap.keys.toSet()

        return object : AnAction(PlatformIcons.ADD_ICON) { /* AddAction */
            override fun actionPerformed(e: AnActionEvent) {
                val fileNameToAdd =
                    (allFileNames - existingFileNames.toSet()).sortedBy { fileTypeMap[it]?.displayName }.firstOrNull()

                fileTypeMap[fileNameToAdd]?.let { addNewFileTypeNode(it) }
            }
        }
    }

    private fun addNewFileTypeNode(fileType: FileType) {
        val profile = FileTypeFontProfile(
            propertyGraph.property(fileType.name),
            propertyGraph.property(true),
            ::existingFileNames,
            FontProfile.createInitialScheme()
        )
        val configurable =
            FileTypeFontConfigurable(profile, AppSettings.FileTypeSettingState(fileType.name), TREE_UPDATER)
        val node = FileTypeSwitcherNode(configurable)

        addNode(node, myRoot)
        selectNodeInTree(node)
    }

    private fun createDeleteAction(): AnAction {
        return object : MyDeleteAction() {
            val deletable: Boolean
                get() = when (val node = selectedNode) {
                    is FileTypeSwitcherNode -> !node.configurable.profile.isBaseProfile
                    else -> true
                }

            override fun update(e: AnActionEvent) {
                super.update(e)

                e.presentation.isEnabled = e.presentation.isEnabled && deletable
            }

            override fun actionPerformed(e: AnActionEvent) {
                if (!deletable) {
                    return
                }

                super.actionPerformed(e)
            }
        }
    }

    abstract class SwitcherNode(open val configurable: FontConfigurable) : MyNode(configurable) {
        override fun getLocationString(): String =
            if (configurable.profile.enabled.get()) message("config.setting.location.enabled.label")
            else message("config.setting.location.disabled.label")
    }

    class FileTypeSwitcherNode(override val configurable: FileTypeFontConfigurable) : SwitcherNode(configurable) {
        override fun getLocationString(): String =
            if (configurable.profile.isBaseProfile) message("config.setting.base.location.label")
            else super.getLocationString()
    }

    override fun isModified(): Boolean {
        return allFileTypeConfigurables.any { it.isModified } // FIXME bug after delete
    }

    override fun apply() {
        val groups = allFileTypeConfigurables.groupBy { it.profile.isBaseProfile }

        state.additional = groups[false]?.map {
            AppSettings.FileTypeSettingState(
                it.profile.fileTypeName.get(), it.profile.enabled.get(), AppSettings.ElementTypeSettingsState().apply {
                    base = AppSettings.ElementTypeSettingState(
                        AppSettings.BASE_ELEMENT_TYPE_NAME, AppSettings.SwitcherFontOptions().apply {
                            update(it.profile.scheme.fontPreferences)
                        })
                })
        }?.toCollection(mutableListOf()) ?: mutableListOf()

        reset()
    }

    override fun reset() {
        myRoot.removeAllChildren()

        val profile = FileTypeFontProfile(
            propertyGraph.property(state.base.fileTypeName),
            propertyGraph.property(state.base.enabled),
            ::existingFileNames,
            FontProfile.createInitialScheme() // Do not insert preference to follow global font
        )
        val baseNode = FileTypeSwitcherNode(FileTypeFontConfigurable(profile, state.base, TREE_UPDATER))
        myRoot.add(baseNode)

        state.additional.forEach {
            val profile = FileTypeFontProfile(
                propertyGraph.property(it.fileTypeName),
                propertyGraph.property(it.enabled),
                ::existingFileNames,
                FontProfile.createInitialScheme().apply {
                    fontPreferences = it.elementTypeSettings.base.options.fontPreferences
                })
            val baseNode = FileTypeSwitcherNode(FileTypeFontConfigurable(profile, it, TREE_UPDATER))
            myRoot.add(baseNode)
        }

        super.reset()
    }

    override fun getNodeComparator(): Comparator<MyNode> = kotlin.Comparator { o1, o2 ->
        if (o1 is FileTypeSwitcherNode && o2 is FileTypeSwitcherNode) when {
            o1.configurable.profile.isBaseProfile -> -1
            o2.configurable.profile.isBaseProfile -> 1
            else -> super.nodeComparator.compare(o1, o2)
        }
        else super.nodeComparator.compare(o1, o2)
    }
}
