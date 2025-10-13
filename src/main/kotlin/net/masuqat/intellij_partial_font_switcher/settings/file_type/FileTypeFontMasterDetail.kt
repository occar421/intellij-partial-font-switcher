package net.masuqat.intellij_partial_font_switcher.settings.file_type

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper.createDefaultBorder
import com.intellij.openapi.ui.MasterDetailsComponent
import com.intellij.openapi.ui.NamedConfigurable
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.dsl.builder.*
import com.intellij.util.PlatformIcons
import com.intellij.util.ui.JBUI
import com.jetbrains.rd.util.Runnable
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.ListCellRenderer

class FileTypeFontMasterDetail : MasterDetailsComponent() {
    init {
        initTree()
    }

    override fun getDisplayName(): @NlsContexts.ConfigurableName String? {
        // TODO from resource
        return "File Type Font"
    }

    override fun createActions(fromPopup: Boolean): List<AnAction> {
        return listOf(
            AddAction(this),
//            MyDeleteAction(),
        )
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
    NamedConfigurable<FileTypeFontProfile>(false, updater) {

    override fun setDisplayName(p0: @NlsSafe String?) {} // No impl.

    override fun getEditableObject(): FileTypeFontProfile = profile

    override fun getBannerSlogan(): @NlsContexts.DetailedDescription String? = null

    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                label("Hoge")
                // TODO Font
            }
        }.apply {
            border = JBUI.Borders.empty(0, 10)
        }
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

}

class FileTypeFontProfile(val fileType: FileType)

private class AddAction(private val masterDetail: FileTypeFontMasterDetail) : AnAction(PlatformIcons.ADD_ICON) {
    var selectedFileType = FileTypeManager.getInstance().registeredFileTypes.first()!!

    override fun actionPerformed(e: AnActionEvent) {
        val dialog = DialogBuilder(e.project).centerPanel(panel {
            row("FileType:") {
                val list: Collection<FileType> = FileTypeManager.getInstance().registeredFileTypes.toList()
                // TODO remove duplicated
                val renderer = ListCellRenderer<FileType?> { list, value, index, isSelected, cellHasFocus ->
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
            masterDetail.addFileTypeFontNode(selectedFileType)
        }
    }
}
