package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.dsl.builder.*
import javax.swing.JComponent

fun Row.configurableCell(c: UnnamedConfigurable): Cell<JComponent> =
    cell(c.createComponent()!!)
        .onIsModified { c.isModified }
        .onApply { c.apply() }
        .onReset { c.reset() }