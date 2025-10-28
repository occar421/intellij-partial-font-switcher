@file:Suppress("UnstableApiUsage")

package net.masuqat.intellij_partial_font_switcher.settings.cloned_components

import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.impl.FontFamilyService
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import net.masuqat.intellij_partial_font_switcher.settings.cloned_components.FontWeightCombo.MyWeightItem
import javax.swing.AbstractListModel
import javax.swing.ComboBoxModel
import javax.swing.JList

/**
 * Clone of com.intellij.application.options.editor.fonts.FontWeightCombo internal API
 */
internal abstract class FontWeightCombo(private val myMarkRecommended: Boolean) : ComboBox<MyWeightItem?>() {
    private val myModel = MyModel()

    init {
        setModel(myModel)
        setRenderer(MyListCellRenderer())
    }

    fun update(fontPreferences: FontPreferences) {
        myModel.update(fontPreferences)
    }

    val selectedSubFamily: String?
        get() = (myModel.selectedItem as? MyWeightItem)?.subFamily

    private inner class MyModel : AbstractListModel<MyWeightItem?>(), ComboBoxModel<MyWeightItem?> {
        private val myItems = mutableListOf<MyWeightItem>()

        private var mySelectedItem: MyWeightItem? = null

        override fun setSelectedItem(anItem: Any?) {
            mySelectedItem = when (anItem) {
                is MyWeightItem -> anItem
                is String -> myItems.find { it.subFamily == anItem }
                else -> return
            }
        }

        override fun getSelectedItem(): Any? = mySelectedItem

        override fun getSize(): Int = myItems.size

        override fun getElementAt(index: Int): MyWeightItem = myItems[index]

        fun update(currPreferences: FontPreferences) {
            myItems.clear()
            val currFamily = currPreferences.fontFamily
            val recommended = getRecommendedSubFamily(currFamily)
            myItems.addAll(FontFamilyService.getSubFamilies(currFamily).map { MyWeightItem(it, it == recommended) })
            val subFamily = getSubFamily(currPreferences)
            setSelectedItem(subFamily ?: recommended)
            fireContentsChanged(this, -1, -1)
        }
    }

    private inner class MyListCellRenderer : ColoredListCellRenderer<MyWeightItem?>() {
        override fun customizeCellRenderer(
            list: JList<out MyWeightItem?>,
            value: MyWeightItem?, index: Int, selected: Boolean, hasFocus: Boolean
        ) {
            if (value != null) {
                append(value.subFamily)
                if (value.isRecommended && myMarkRecommended && list.getModel().size > 2) {
                    append("  ")
                    append(
                        ApplicationBundle.message("settings.editor.font.recommended"),
                        SimpleTextAttributes.GRAY_ATTRIBUTES
                    )
                }
            }
        }
    }

    internal class MyWeightItem(internal val subFamily: @NlsSafe String, internal val isRecommended: Boolean)

    abstract fun getSubFamily(preferences: FontPreferences): String?

    abstract fun getRecommendedSubFamily(family: String): String
}