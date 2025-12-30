package net.masuqat.intellij_partial_font_switcher.editor_mod

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.markup.CustomHighlighterRenderer
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints

val selector = FileTypeFontPreferenceSelector()
val PARTIAL_FONT_HIGHLIGHTERS_KEY = Key.create<MutableList<RangeHighlighter>>("PARTIAL_FONT_HIGHLIGHTERS")

fun Editor.switchFontPreference() {
    if (this.virtualFile != null) {
        val fontPreferences = selector.select(this.virtualFile!!.fileType)
        this.colorsScheme.fontPreferences = fontPreferences
        this.colorsScheme.lineSpacing = fontPreferences.lineSpacing
        
        this.applyPartialFont()
    }
}

fun Editor.setupPartialFontListener() {
    val project = this.project ?: return
    this.document.addDocumentListener(object : DocumentListener {
        override fun documentChanged(event: DocumentEvent) {
            PsiDocumentManager.getInstance(project).performWhenAllCommitted {
                applyPartialFont()
            }
        }
    }, project)
}

fun Editor.revertFontPreference() {
    this.colorsScheme.fontPreferences = selector.globalFontPreference
    this.colorsScheme.lineSpacing = selector.globalFontPreference.lineSpacing
    clearPartialFontHighlighters()
}

private fun Editor.clearPartialFontHighlighters() {
    this.getUserData(PARTIAL_FONT_HIGHLIGHTERS_KEY)?.forEach {
        this.markupModel.removeHighlighter(it)
    }
    this.putUserData(PARTIAL_FONT_HIGHLIGHTERS_KEY, null)
}

fun Editor.applyPartialFont() {
    val project = this.project ?: return
    val virtualFile = this.virtualFile ?: return
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(this.document) ?: return

    val elementTypeName = "KDOC_SECTION"
    val fontPreferences = selector.selectForElement(virtualFile.fileType, elementTypeName)
    
    val fontName = fontPreferences?.fontFamily ?: "Impact"
    val customFont = Font(fontName, Font.PLAIN, this.colorsScheme.editorFontSize)

    // クリーンアップ
    clearPartialFontHighlighters()

    val targetRanges = mutableListOf<TextRange>()
    psiFile.accept(object : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (element.node.elementType.toString().contains(elementTypeName)) {
                targetRanges.add(element.textRange)
            }
        }
    })

    val highlighters = mutableListOf<RangeHighlighter>()
    
    // 元のテキストを隠すための属性（透明色を使用）
    val attributes = TextAttributes()
    attributes.foregroundColor = Color(0, 0, 0, 0)

    targetRanges.forEach { range ->
        val highlighter = this.markupModel.addRangeHighlighter(
            range.startOffset,
            range.endOffset,
            HighlighterLayer.ADDITIONAL_SYNTAX,
            attributes,
            HighlighterTargetArea.EXACT_RANGE
        )
        highlighter.customRenderer = PartialFontHighlighterRenderer(customFont)
        highlighters.add(highlighter)
    }
    
    this.putUserData(PARTIAL_FONT_HIGHLIGHTERS_KEY, highlighters)
}

class PartialFontHighlighterRenderer(val font: Font) : CustomHighlighterRenderer {
    override fun paint(editor: Editor, highlighter: RangeHighlighter, g: Graphics) {
        val start = highlighter.startOffset
        val end = highlighter.endOffset
        if (start >= end || start >= editor.document.textLength) return
        
        val actualEnd = end.coerceAtMost(editor.document.textLength)
        val text = editor.document.getText(TextRange(start, actualEnd))
        
        val g2 = g as? Graphics2D ?: return
        val oldFont = g2.font
        val oldColor = g2.color
        val oldHints = g2.renderingHints
        
        g2.font = font
        g2.color = editor.colorsScheme.defaultForeground
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)
        
        val ascent = editor.ascent
        
        var i = 0
        while (i < text.length) {
            val char = text[i]
            if (char == '\n' || char == '\r' || char == '\t') {
                i++
                continue
            }

            var j = i
            while (j < text.length && text[j] != '\n' && text[j] != '\r' && text[j] != '\t') {
                j++
            }

            val chunk = text.substring(i, j)
            // Use visualPositionToXY to handle soft wraps and folding correctly for the start of the chunk
            val point = editor.visualPositionToXY(editor.offsetToVisualPosition(start + i))
            g2.drawString(chunk, point.x, point.y + ascent)

            i = j
        }
        
        g2.font = oldFont
        g2.color = oldColor
        g2.setRenderingHints(oldHints)
    }
}
