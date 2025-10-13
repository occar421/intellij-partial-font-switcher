package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.util.NlsContexts
import com.intellij.ui.dsl.builder.*
import net.masuqat.intellij_partial_font_switcher.settings.file_type.FileTypeConfig
import javax.swing.JComponent

class AppConfig : Configurable, Configurable.Beta {
    override fun getDisplayName(): @NlsContexts.ConfigurableName String {
        return "Partial Font Switcher" // TODO: from resource
    }

    class Model {
        val propertyGraph = PropertyGraph()
    }

    val model = Model()
    val fileTypeConfig = FileTypeConfig(model.propertyGraph)

    private val panel = panel {
        collapsibleGroup("FileType Level") { // TODO: from resource
            row {
                cell(fileTypeConfig.createComponent())
                    .align(AlignX.FILL)
                    .onIsModified { fileTypeConfig.isModified }
            }

        }.apply {
            expanded = true
        }
    }

    override fun createComponent(): JComponent {
        return panel
    }

    override fun isModified(): Boolean {
        return panel.isModified()
    }

    override fun apply() {
        panel.apply()

        // TODO apply 後にエディタ自体に反映させる
    }

    override fun reset() {
        panel.reset()
    }
}
