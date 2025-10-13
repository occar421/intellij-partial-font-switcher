package net.masuqat.intellij_partial_font_switcher.settings

import com.intellij.openapi.options.UnnamedConfigurable
import com.intellij.ui.TableUtil
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer

class LanguageFontTableModel() : AbstractTableModel(), UnnamedConfigurable {
    private data class Column(val name: String, val type: Class<*>) // TODO from resource

    private val columns = arrayOf(
        Column("Language", String::class.java),
        Column("Font", String::class.java),
    )

    data class LanguageFont(val language: String, val font: String)

    val model = mutableListOf<LanguageFont>()

    override fun isModified(): Boolean {
        return false
        // TODO("Not yet implemented")
    }

    override fun apply() {
        // TODO("Not yet implemented")
    }

    override fun reset() {
        // TODO("Not yet implemented")
        this.model.clear()

        fireTableDataChanged()
    }

    override fun createComponent(): JComponent {
        val table = JBTable(this)
        table.emptyText.text = "No language fonts"

        val languageColumn = table.columnModel.getColumn(0)
        languageColumn.cellRenderer = DefaultTableCellRenderer()

        return ToolbarDecorator.createDecorator(table)
            .setAddAction {
                model.add(LanguageFont("Aiueo", "Impact2"))
                val index = model.size - 1
                fireTableRowsInserted(index, index)
                table.getSelectionModel().setSelectionInterval(index, index)
                table.scrollRectToVisible(table.getCellRect(index, 0, true))
            }
            .setRemoveAction {
                val selectedRow = table.selectedRow
                if (selectedRow != -1) {
                    model.removeAt(selectedRow)
                    fireTableRowsDeleted(selectedRow, selectedRow)
                }
                if (model.isNotEmpty()) {
                    val row = kotlin.math.min(selectedRow, model.size - 1)
                    table.getSelectionModel().setSelectionInterval(row, row)
                }
                TableUtil.updateScroller(table)
            }
            .createPanel()
    }

    override fun getRowCount() = model.size

    override fun getColumnClass(columnIndex: Int) = columns[columnIndex].type

    override fun getColumnCount() = columns.size

    override fun getColumnName(column: Int) = columns[column].name

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        return when (columnIndex) {
            0 -> model[rowIndex].language
            1 -> model[rowIndex].font
            else -> throw IndexOutOfBoundsException("columnIndex: $columnIndex")
        }
    }

    // TODO appState
}