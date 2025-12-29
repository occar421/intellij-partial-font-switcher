package net.masuqat.intellij_partial_font_switcher.editor_mod

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementVisitor
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle

val selector = FileTypeFontPreferenceSelector()

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
    this.inlayModel.getBlockElementsInRange(0, this.document.textLength, PartialFontInlayRenderer::class.java)
        .forEach { it.dispose() }

    val targetRanges = mutableListOf<TextRange>()
    psiFile.accept(object : PsiRecursiveElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (element.node.elementType.toString().contains(elementTypeName)) {
                targetRanges.add(element.textRange)
            }
        }
    })

    this.foldingModel.runBatchFoldingOperation {
        targetRanges.forEach { range ->
            // 既存の折りたたみをチェック（通常の FoldRegion）
            val regions = this.foldingModel.allFoldRegions.filter { 
                it.startOffset == range.startOffset && it.endOffset == range.endOffset 
            }
            if (regions.isEmpty()) {
                val region = this.foldingModel.addFoldRegion(range.startOffset, range.endOffset, "")
                region?.isExpanded = false
            } else {
                regions.forEach { it.isExpanded = false }
            }

            val text = this.document.getText(range)
            val renderer = PartialFontInlayRenderer(text, customFont)
            this.inlayModel.addInlineElement(range.startOffset, renderer)
        }
    }
}

class PartialFontInlayRenderer(val text: String, val font: Font) : EditorCustomElementRenderer {
    override fun calcWidthInPixels(inlay: com.intellij.openapi.editor.Inlay<*>): Int {
        val metrics = inlay.editor.component.getFontMetrics(font)
        return text.lines().maxOfOrNull { metrics.stringWidth(it) } ?: 0
    }

    override fun calcHeightInPixels(inlay: com.intellij.openapi.editor.Inlay<*>): Int {
        return inlay.editor.lineHeight * text.lines().size
    }

    override fun paint(
        inlay: com.intellij.openapi.editor.Inlay<*>,
        g: Graphics,
        targetRegion: Rectangle,
        textAttributes: TextAttributes
    ) {
        g.font = font
        g.color = textAttributes.foregroundColor
        val lineHeight = inlay.editor.lineHeight
        val ascent = inlay.editor.ascent

        text.lines().forEachIndexed { index, line ->
            g.drawString(line, targetRegion.x, targetRegion.y + index * lineHeight + ascent)
        }
    }
}
