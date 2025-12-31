package net.masuqat.intellij_partial_font_switcher.editor_mod

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.view.VisualLinesIterator
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
import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import org.mockito.kotlin.doReturn
import java.awt.Color
import java.awt.Composite
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.Image
import java.awt.Paint
import java.awt.Rectangle
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.Stroke
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp
import java.awt.image.ImageObserver
import java.awt.image.RenderedImage
import java.awt.image.renderable.RenderableImage
import java.text.AttributedCharacterIterator
import org.mockito.kotlin.mock

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

    val elementTypeName = "KDOC"
    val fontPreferences = selector.selectForElement(virtualFile.fileType, elementTypeName)

    val fontName = fontPreferences?.fontFamily ?: "Cascadia Code"
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

//    com.intellij.openapi.editor.impl.view.LineLayout
    val view = (this as EditorImpl).view
//    (this as EditorImpl).editorModel.documentMarkupModel.
    val mockG = mock<Graphics2D> {
        on { clipBounds } doReturn Rectangle(0, 0, 100, 100)
        on { transform } doReturn AffineTransform.getScaleInstance(1.0, 1.0)
        on { color } doReturn Gray.TRANSPARENT
        on { fontRenderContext } doReturn FontRenderContext(AffineTransform(), true, true)
        on { create() } doReturn this.mock
    }
    view.paint(mockG)
//
    // 元のテキストを隠すための属性（透明色を使用）
    val attributes = TextAttributes()
    attributes.foregroundColor = Gray.TRANSPARENT

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

        val oldFont = g.font
        val oldColor = g.color

        g.font = font
        g.color = editor.colorsScheme.defaultForeground

        val ascent = editor.ascent

        // FIXME use Editor's drawing method w/ colors ref LineLayout
        for (i in text.indices) {
            val offset = start + i
            val char = text[i]
            if (char == '\n' || char == '\r' || char == '\t') continue

            val visualPosition = editor.offsetToVisualPosition(offset)
            val point = editor.visualPositionToXY(visualPosition)

            g.drawString(char.toString(), point.x, point.y + ascent)
        }

        g.font = oldFont
        g.color = oldColor
    }
}
