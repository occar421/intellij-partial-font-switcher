package net.masuqat.intellij_partial_font_switcher.settings.cloned_components

import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.impl.FontFamilyService
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.*
import com.intellij.ui.components.JBLabel
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.util.*
import java.util.function.Consumer
import javax.accessibility.AccessibleContext
import javax.swing.AbstractListModel
import javax.swing.ComboBoxModel
import javax.swing.JList
import javax.swing.JPanel

/**
 * Clone of com.intellij.application.options.colors.FontFamilyCombo internal API
 */
internal class FontFamilyCombo(isPrimary: Boolean) :
    AbstractFontCombo<FontFamilyCombo.MyFontItem?>(MyModel(!isPrimary)) {
    private val myIsPrimary: Boolean

    init {
        setSwingPopup(false)
        myIsPrimary = isPrimary
        ClientProperty.put<Boolean?>(this, AnimatedIcon.ANIMATION_IN_RENDERER_ALLOWED, true)
        setRenderer(object : GroupedComboBoxRenderer<MyFontItem?>(this) {
            override fun customize(
                item: SimpleColoredComponent,
                value: MyFontItem?,
                index: Int,
                isSelected: Boolean,
                hasFocus: Boolean
            ) {
                if (value != null) {
                    if (value is MyWarningItem) {
                        item.append(value.familyName, SimpleTextAttributes.ERROR_ATTRIBUTES)
                        return
                    }
                    var attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES
                    if (index > -1 && value.myFont != null) {
                        if (value.myFontCanDisplayName) {
                            item.setFont(value.myFont)
                        } else if (myIsPrimary) {
                            attributes = SimpleTextAttributes.EXCLUDED_ATTRIBUTES
                        }
                    } else {
                        item.setFont(JBUI.Fonts.label())
                    }
                    item.append(value.familyName, attributes)
                }
            }

            override fun separatorFor(value: MyFontItem?): ListSeparator? {
                val m = getModel()
                if (m is MyModel) {
                    if (!m.myItems.isEmpty() && ContainerUtil.find<MyFontItem?>(
                            m.myItems,
                            Condition { item: MyFontItem? -> item!!.myIsMonospaced }) === value
                    ) return ListSeparator(ApplicationBundle.message("settings.editor.font.monospaced"))
                    if (!m.myItems.isEmpty() && ContainerUtil.find<MyFontItem?>(
                            m.myItems,
                            Condition { item: MyFontItem? -> !item!!.myIsMonospaced && item !is MyNoFontItem }) === value
                    ) return ListSeparator(ApplicationBundle.message("settings.editor.font.proportional"))
                }
                return null
            }

            override fun getListCellRendererComponent(
                list: JList<out MyFontItem?>?,
                value: MyFontItem?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                if (index != -1 || !(dataModel as MyModel).isUpdating) {
                    return component
                } else {
                    val panel: JPanel = object : CellRendererPanel(BorderLayout()) {
                        override fun getAccessibleContext(): AccessibleContext? {
                            return component.getAccessibleContext()
                        }
                    }
                    component.setBackground(null)
                    panel.add(component, BorderLayout.CENTER)
                    val progressIcon = JBLabel(AnimatedIcon.Default.INSTANCE)
                    panel.add(progressIcon, BorderLayout.EAST)
                    return panel
                }
            }
        })
    }

    override fun getFontName(): @NlsSafe String? {
        val selectedItem = getModel().getSelectedItem()
        return if (selectedItem is MyFontItem) selectedItem.familyName else null
    }

    override fun setFontName(fontName: @NlsSafe String?) {
        getModel().setSelectedItem(fontName)
    }

    override fun isNoFontSelected(): Boolean {
        return getModel().getSelectedItem() is MyNoFontItem
    }

    // region Not supported by this implementation
    override fun setMonospacedOnly(isMonospacedOnly: Boolean) {
        // Ignored
    }

    override fun isMonospacedOnly(): Boolean {
        return false
    }

    override fun isMonospacedOnlySupported(): Boolean {
        return false
    }


    // endregion
    internal open class MyFontItem(val familyName: String, internal var myIsMonospaced: Boolean) {
        internal var myFontCanDisplayName = false
        internal var myFont: Font? = null

        override fun toString(): String {
            return this.familyName
        }

        val isSelectable: Boolean
            get() = true
    }

    private class MyNoFontItem : MyFontItem("<None>", false)

    private class MyWarningItem(missingName: String) :
        MyFontItem(ApplicationBundle.message("settings.editor.font.missing.custom.font", missingName), false)

    private class MyModel(withNoneItem: Boolean) : AbstractListModel<MyFontItem?>(), ComboBoxModel<MyFontItem?> {
        var isUpdating: Boolean = true
            private set
        private val myMonospacedFamilies: MutableSet<String?> = HashSet<String?>()
        internal val myItems: MutableList<MyFontItem> = ArrayList<MyFontItem>()
        private val myNoFontItem: MyNoFontItem?
        private var mySelectedItem: MyFontItem? = null

        init {
            myMonospacedFamilies.addAll(Arrays.asList<String?>(*KNOWN_MONOSPACED_FAMILIES))
            if (withNoneItem) {
                myNoFontItem = MyNoFontItem()
                myItems.add(myNoFontItem)
            } else {
                myNoFontItem = null
            }
            FontFamilyService.getAvailableFamilies().forEach(
                Consumer { name: String? -> myItems.add(MyFontItem(name!!, myMonospacedFamilies.contains(name))) }
            )
            Collections.sort<MyFontItem?>(myItems, MyFontItemComparator())
            retrieveFontInfo()
        }

        override fun setSelectedItem(anItem: Any?) {
            if (anItem == null) {
                mySelectedItem = myNoFontItem
            } else if (anItem is String) {
                mySelectedItem = ContainerUtil.find<MyFontItem?>(
                    myItems,
                    Condition { item: MyFontItem? -> item!!.isSelectable && item.familyName == anItem })
                if (mySelectedItem == null) {
                    mySelectedItem = MyWarningItem(anItem)
                }
            } else if (anItem is MyFontItem) {
                mySelectedItem = anItem
            }
            fireContentsChanged(this, -1, -1)
        }

        override fun getSelectedItem(): MyFontItem? {
            return mySelectedItem
        }

        override fun getSize(): Int {
            return myItems.size
        }

        override fun getElementAt(index: Int): MyFontItem? {
            return myItems.get(index)
        }

        fun retrieveFontInfo() {
            ApplicationManager.getApplication().executeOnPooledThread(Runnable {
                for (item in myItems) {
                    if (FontFamilyService.isMonospaced(item.familyName)) {
                        myMonospacedFamilies.add(item.familyName)
                    }
                    item.myFont = JBUI.Fonts.create(item.familyName, FontPreferences.DEFAULT_FONT_SIZE)
                    item.myFontCanDisplayName = item.myFont!!.canDisplayUpTo(item.familyName) == -1
                }
                updateMonospacedInfo()
            })
        }

        fun updateMonospacedInfo() {
            ApplicationManager.getApplication().invokeLater(
                Runnable {
                    for (item in myItems) {
                        item.myIsMonospaced = myMonospacedFamilies.contains(item.familyName)
                    }
                    if (myNoFontItem == null) { // Primary font
                        myItems.removeIf { item: MyFontItem? -> !item!!.myFontCanDisplayName }
                    }
                    this.isUpdating = false
                    Collections.sort<MyFontItem?>(myItems, MyFontItemComparator())
                    fireContentsChanged(this, -1, -1)
                }, ModalityState.any()
            )
        }

        companion object {
            /**
             * The list contains bundled fonts and platform-specific default fonts specified in
             * [FontPreferences].
             * It is used for quick filtering of monospaced fonts before the actual list is shown.
             */
            private val KNOWN_MONOSPACED_FAMILIES = arrayOf<String?>(
                "Consolas",
                "DejaVu Sans Mono",
                "Droid Sans Mono",
                "JetBrains Mono",
                "Fira Code",
                "Inconsolata",
                "Menlo",
                "Monospaced",
                "Source Code Pro"
            )
        }
    }

    private class MyFontItemComparator : Comparator<MyFontItem?> {
        override fun compare(item1: MyFontItem?, item2: MyFontItem?): Int {
            if (item1!! is MyNoFontItem) return -1
            if (item2!! is MyNoFontItem) return 1
            if (item1.myIsMonospaced && !item2.myIsMonospaced) return -1
            if (!item1.myIsMonospaced && item2.myIsMonospaced) return 1
            return item1.familyName.compareTo(item2.familyName)
        }

        override fun equals(obj: Any?): Boolean {
            return false
        }
    }

    companion object {
        val maxWidth: Int = 230
    }
}