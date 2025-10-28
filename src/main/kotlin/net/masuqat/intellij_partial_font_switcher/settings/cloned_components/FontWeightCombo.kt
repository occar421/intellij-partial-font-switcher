// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package net.masuqat.intellij_partial_font_switcher.settings.cloned_components

import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.editor.colors.FontPreferences
import com.intellij.openapi.editor.impl.FontFamilyService
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.containers.ContainerUtil
import net.masuqat.intellij_partial_font_switcher.settings.cloned_components.FontWeightCombo.MyWeightItem
import java.util.function.Consumer
import javax.swing.AbstractListModel
import javax.swing.ComboBoxModel
import javax.swing.JList

/**
 * Clone of com.intellij.application.options.editor.fonts.FontWeightCombo internal API
 */
internal abstract class FontWeightCombo(private val myMarkRecommended: Boolean) : ComboBox<MyWeightItem?>() {
    private val myModel: MyModel

    init {
        myModel = MyModel()
        setModel(myModel)
        setRenderer(MyListCellRenderer())
    }

    fun update(fontPreferences: FontPreferences) {
        myModel.update(fontPreferences)
    }

    val selectedSubFamily: String?
        get() {
            val selected = myModel.getSelectedItem()
            return if (selected is MyWeightItem) selected.subFamily else null
        }

    private inner class MyModel : AbstractListModel<MyWeightItem?>(), ComboBoxModel<MyWeightItem?> {
        private val myItems: MutableList<MyWeightItem?> = ArrayList<MyWeightItem?>()

        private var mySelectedItem: MyWeightItem? = null

        override fun setSelectedItem(anItem: Any?) {
            if (anItem is MyWeightItem) {
                mySelectedItem = anItem
            } else if (anItem is String) {
                mySelectedItem = ContainerUtil.find<MyWeightItem?>(
                    myItems,
                    Condition { item: MyWeightItem? -> item!!.subFamily == anItem })
            }
        }

        override fun getSelectedItem(): Any? {
            return mySelectedItem
        }

        override fun getSize(): Int {
            return myItems.size
        }

        override fun getElementAt(index: Int): MyWeightItem? {
            return myItems.get(index)
        }

        fun update(currPreferences: FontPreferences) {
            myItems.clear()
            val currFamily = currPreferences.getFontFamily()
            val recommended = getRecommendedSubFamily(currFamily)
            FontFamilyService.getSubFamilies(currFamily).forEach(
                Consumer { subFamily: String? -> myItems.add(MyWeightItem(subFamily!!, subFamily == recommended)) }
            )
            val subFamily = getSubFamily(currPreferences)
            setSelectedItem(if (subFamily != null) subFamily else recommended)
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
                if (value.isRecommended && myMarkRecommended && list.getModel().getSize() > 2) {
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