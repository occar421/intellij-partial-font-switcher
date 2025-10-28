package net.masuqat.intellij_partial_font_switcher.settings.cloned_components

import com.intellij.application.options.colors.ColorAndFontSettingsListener
import com.intellij.application.options.colors.FontPreviewService
import com.intellij.application.options.colors.PreviewPanel
import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.openapi.actionSystem.ShortcutSet
import com.intellij.openapi.actionSystem.remoting.ActionRemoteBehaviorSpecification
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.EditorGutterComponentEx
import com.intellij.openapi.editor.ex.EditorMarkupModel
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.editor.highlighter.HighlighterClient
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.openapi.editor.impl.ContextMenuPopupHandler
import com.intellij.openapi.editor.markup.AnalyzerStatus
import com.intellij.openapi.editor.markup.ErrorStripeRenderer
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.editor.markup.UIController
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerBase
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.JBColor
import com.intellij.util.Consumer
import com.intellij.util.EventDispatcher
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jetbrains.annotations.Nls
import java.awt.BorderLayout
import java.awt.Font
import java.util.function.Supplier
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.Border
import kotlin.math.max
import kotlin.math.min

/**
 * Clone of com.intellij.application.options.colors.FontEditorPreview internal API
 */
class FontEditorPreview(private val mySchemeSupplier: Supplier<out EditorColorsScheme?>, editable: Boolean) :
    PreviewPanel {
    private val myEditor: EditorEx
    private val myTopPanel: JPanel

    private val myTextModel: PreviewTextModel

    private val myDispatcher =
        EventDispatcher.create<ColorAndFontSettingsListener?>(ColorAndFontSettingsListener::class.java)

    init {
        @Nls val text = PropertiesComponent.getInstance().getValue(
            PREVIEW_TEXT_KEY,
            iDEDemoText
        )

        myTextModel = PreviewTextModel(text)
        myEditor = createPreviewEditor(myTextModel.text, mySchemeSupplier.get(), editable) as EditorEx
        myEditor.setBorder(JBUI.Borders.empty())
        myEditor.setHighlighter(PreviewHighlighter(myTextModel, myEditor.getDocument()))
        myEditor.getDocument().addDocumentListener(myTextModel)

        myTopPanel = JPanel(BorderLayout())
        myTopPanel.add(myEditor.getComponent(), BorderLayout.CENTER)

        if (editable) {
            val previewLabel = JLabel(ApplicationBundle.message("settings.editor.font.preview.hint"))
            previewLabel.setFont(JBUI.Fonts.smallFont())
            previewLabel.setForeground(UIUtil.getContextHelpForeground())
            previewLabel.setBorder(JBUI.Borders.empty(10, 15, 10, 0))
            previewLabel.setBackground(myEditor.getBackgroundColor())
            myTopPanel.add(previewLabel, BorderLayout.SOUTH)
        }
        myTopPanel.setBackground(myEditor.getBackgroundColor())
        myTopPanel.setBorder(this.border)

        registerActions(myEditor)
        installTrafficLights(myEditor)
    }

    protected val border: Border
        get() = JBUI.Borders.customLine(JBColor.border())

    private fun registerActions(editor: EditorEx) {
        editor.putUserData<PreviewTextModel?>(TEXT_MODEL_KEY, myTextModel)
        val restoreAction = ActionManager.getInstance().getAction(IdeActions.ACTION_RESTORE_FONT_PREVIEW_TEXT)
        val toggleBoldFontAction = ActionManager.getInstance().getAction("fontEditorPreview.ToggleBoldFont")
        if (restoreAction != null || toggleBoldFontAction != null) {
            val originalGroupId = editor.getContextMenuGroupId()
            val originalGroup =
                if (originalGroupId == null) null else ActionManager.getInstance().getAction(originalGroupId)
            val group = DefaultActionGroup()
            if (originalGroup is ActionGroup) {
                group.addAll(originalGroup)
            }
            if (restoreAction != null) {
                group.add(restoreAction)
            }
            if (toggleBoldFontAction != null) {
                group.add(toggleBoldFontAction)
                DumbAwareAction.create(Consumer { event: AnActionEvent? -> toggleBoldFont(editor) })
                    .registerCustomShortcutSet(
                        TOGGLE_BOLD_SHORTCUT, editor.getComponent()
                    )
            }
            editor.installPopupHandler(ContextMenuPopupHandler.Simple(group))
        }
    }

    override fun getPanel(): JComponent {
        return myTopPanel
    }

    override fun updateView() {
        val scheme = updateOptionsScheme(mySchemeSupplier.get())

        myEditor.setColorsScheme(scheme)
        myEditor.reinitSettings()
    }

    protected fun updateOptionsScheme(selectedScheme: EditorColorsScheme): EditorColorsScheme {
        return selectedScheme
    }

    override fun blinkSelectedHighlightType(description: Any?) {
    }

    override fun addListener(listener: ColorAndFontSettingsListener) {
        myDispatcher.addListener(listener)
    }

    override fun disposeUIResources() {
        if (myTextModel.isDefault || myTextModel.rawText.isEmpty()) {
            PropertiesComponent.getInstance().unsetValue(PREVIEW_TEXT_KEY)
        } else {
            PropertiesComponent.getInstance().setValue(
                PREVIEW_TEXT_KEY,
                myTextModel.rawText
            )
        }
        EditorFactory.getInstance().releaseEditor(myEditor)
    }

    private class DumbTrafficLightRenderer : ErrorStripeRenderer {
        override fun getStatus(): AnalyzerStatus {
            return AnalyzerStatus(AllIcons.General.InspectionsOK, "", "", UIController.EMPTY)
        }
    }

    internal class RestorePreviewTextAction : DumbAwareAction() {
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

        override fun update(e: AnActionEvent) {
            val editor = e.getData<Editor?>(CommonDataKeys.EDITOR)
            val textModel: PreviewTextModel? = if (editor == null) null else editor.getUserData<PreviewTextModel?>(
                TEXT_MODEL_KEY
            )
            e.getPresentation().setEnabledAndVisible(editor != null && textModel != null && !textModel.isDefault)
        }

        override fun actionPerformed(e: AnActionEvent) {
            val editor = e.getData<Editor?>(CommonDataKeys.EDITOR)
            if (editor != null) {
                val textModel: PreviewTextModel? = editor.getUserData<PreviewTextModel?>(TEXT_MODEL_KEY)
                if (textModel != null) {
                    textModel.resetToDefault()
                    WriteCommandAction.runWriteCommandAction(editor.getProject(), null, null, Runnable {
                        editor.getDocument().setText(textModel.text)
                        (editor as EditorEx).reinitSettings()
                    })
                }
            }
        }
    }

    internal class ToggleBoldFontAction : DumbAwareAction(), ActionRemoteBehaviorSpecification.Frontend {
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }

        override fun update(e: AnActionEvent) {
            val editor = e.getData<Editor?>(CommonDataKeys.EDITOR)
            val textModel: PreviewTextModel? = if (editor == null) null else editor.getUserData<PreviewTextModel?>(
                TEXT_MODEL_KEY
            )
            e.getPresentation().setEnabledAndVisible(textModel != null && editor!!.getSelectionModel().hasSelection())
        }

        override fun actionPerformed(e: AnActionEvent) {
            val editor = e.getData<Editor?>(CommonDataKeys.EDITOR)
            if (editor != null) {
                toggleBoldFont(editor as EditorEx)
            }
        }
    }

    private class PreviewTextModel(rawPreviewText: String) : DocumentListener {
        private var myText: String? = null
        private val myRanges: MutableList<RangeHighlightingData> = ArrayList<RangeHighlightingData>()

        init {
            extractMarkersAndText(rawPreviewText)
        }

        fun extractMarkersAndText(rawPreviewText: String) {
            myRanges.clear()
            val output = StringBuilder()
            var shift = 0
            var searchOffset = 0
            while (searchOffset < rawPreviewText.length) {
                val rawBoldStart = rawPreviewText.indexOf(BOLD_START_MARKER, searchOffset)
                if (rawBoldStart >= 0) {
                    output.append(rawPreviewText, searchOffset, rawBoldStart)
                    val boldStart = rawBoldStart - shift
                    myRanges.add(RangeHighlightingData(searchOffset - shift, boldStart, false))
                    searchOffset = rawBoldStart + BOLD_START_MARKER.length
                    shift += BOLD_START_MARKER.length

                    var rawBoldEnd = rawPreviewText.indexOf(BOLD_END_MARKER, searchOffset)
                    if (rawBoldEnd < 0) rawBoldEnd = rawPreviewText.length
                    output.append(rawPreviewText, searchOffset, rawBoldEnd)
                    val boldEnd = rawBoldEnd - shift
                    searchOffset = rawBoldEnd + BOLD_END_MARKER.length
                    shift += BOLD_END_MARKER.length
                    myRanges.add(RangeHighlightingData(boldStart, boldEnd, true))
                } else {
                    myRanges.add(RangeHighlightingData(searchOffset - shift, rawPreviewText.length - shift, false))
                    output.append(rawPreviewText, searchOffset, rawPreviewText.length)
                    break
                }
            }
            myText = output.toString()
        }

        val text: String
            get() = myText!!

        val rawText: String
            get() {
                val builder = StringBuilder()
                for (data in myRanges) {
                    if (data.isBold) {
                        builder.append(BOLD_START_MARKER)
                    }
                    builder.append(myText, data.textRange.getStartOffset(), data.textRange.getEndOffset())
                    if (data.isBold) {
                        builder.append(BOLD_END_MARKER)
                    }
                }
                return builder.toString()
            }

        val rangeCount: Int
            get() = myRanges.size

        fun getRangeDataAt(index: Int): RangeHighlightingData? {
            return if (myRanges.isEmpty()) null else myRanges.get(index)
        }

        val isDefault: Boolean
            get() = iDEDemoText == this.rawText

        fun resetToDefault() {
            extractMarkersAndText(iDEDemoText)
        }

        override fun documentChanged(event: DocumentEvent) {
            val docText = event.getDocument().getText()
            if (myText == docText) return
            if (event.isWholeTextReplaced()) {
                myRanges.clear()
                myRanges.add(RangeHighlightingData(0, docText.length, false))
            } else {
                val offset = event.getOffset()
                if (event.getNewLength() >= event.getOldLength()) {
                    if (myRanges.isEmpty()) {
                        myRanges.add(RangeHighlightingData(0, 0, false))
                    }
                    val insertedLen = event.getNewLength() - event.getOldLength()
                    for (data in myRanges) {
                        if (data.textRange.contains(offset) || data == myRanges.get(myRanges.size - 1) && data.textRange.getEndOffset() <= offset) {
                            data.updateRange(data.textRange.grown(insertedLen))
                        } else if (data.textRange.getStartOffset() > offset) {
                            data.updateRange(data.textRange.shiftRight(insertedLen))
                        }
                    }
                } else {
                    val deletedLen = event.getOldLength() - event.getNewLength()
                    val deletedRange = TextRange(offset, offset + deletedLen)
                    var delta = 0
                    val rangeIterator = myRanges.iterator()
                    while (rangeIterator.hasNext()) {
                        val data = rangeIterator.next()
                        val cutoutStart = max(deletedRange.getStartOffset(), data.textRange.getStartOffset())
                        val cutoutEnd = min(deletedRange.getEndOffset(), data.textRange.getEndOffset())
                        if (cutoutStart < cutoutEnd) {
                            val shrinkSize = cutoutEnd - cutoutStart
                            if (shrinkSize == data.textRange.getLength()) {
                                rangeIterator.remove()
                            } else {
                                data.updateRange(data.textRange.grown(-shrinkSize))
                            }
                            data.updateRange(data.textRange.shiftLeft(delta))
                            delta += shrinkSize
                        } else {
                            data.updateRange(data.textRange.shiftLeft(delta))
                        }
                    }
                }
            }
            myText = docText
        }

        fun getIndexAtOffset(offset: Int): Int {
            for (i in myRanges.indices) {
                if (myRanges.get(i).textRange.contains(offset)) return i
            }
            return -1
        }

        fun toggleBoldFont(toggleRange: TextRange) {
            val updatedRanges: MutableList<RangeHighlightingData?> = ArrayList<RangeHighlightingData?>()
            myRanges.forEach(java.util.function.Consumer { data: RangeHighlightingData? ->
                val toggleStart = max(toggleRange.getStartOffset(), data!!.textRange.getStartOffset())
                val toggleEnd = min(toggleRange.getEndOffset(), data.textRange.getEndOffset())
                if (toggleStart < toggleEnd) {
                    glueRange(
                        updatedRanges,
                        TextRange.create(data.textRange.getStartOffset(), toggleStart),
                        data.isBold
                    )
                    glueRange(updatedRanges, TextRange.create(toggleStart, toggleEnd), !data.isBold)
                    glueRange(updatedRanges, TextRange.create(toggleEnd, data.textRange.getEndOffset()), data.isBold)
                } else {
                    glueRange(updatedRanges, data.textRange, data.isBold)
                }
            })
            myRanges.clear()
            myRanges.addAll(updatedRanges.filterNotNull())
        }

        companion object {
            private const val BOLD_START_MARKER = "<bold>"
            private const val BOLD_END_MARKER = "</bold>"

            private fun glueRange(ranges: MutableList<RangeHighlightingData?>, range: TextRange, isBold: Boolean) {
                if (!range.isEmpty()) {
                    val lastRange = if (ranges.isEmpty()) null else ranges.get(ranges.size - 1)
                    if (lastRange != null && lastRange.isBold == isBold) {
                        lastRange.updateRange(lastRange.textRange.grown(range.getLength()))
                    } else {
                        ranges.add(RangeHighlightingData(range.getStartOffset(), range.getEndOffset(), isBold))
                    }
                }
            }
        }
    }

    private class RangeHighlightingData(startOffset: Int, endOffset: Int, val isBold: Boolean) {
        var textRange: TextRange

        init {
            textRange = TextRange.create(startOffset, endOffset)
        }

        fun updateRange(newRange: TextRange) {
            this.textRange = newRange
        }

        override fun toString(): String {
            return "RangeHighlightingData{" +
                    "textRange=" + textRange +
                    ", isBold=" + isBold +
                    '}'
        }
    }

    private class PreviewHighlighter(private val myTextModelModel: PreviewTextModel, private val myDocument: Document) :
        EditorHighlighter {
        override fun createIterator(startOffset: Int): HighlighterIterator {
            return PreviewHighlighterIterator(myTextModelModel, myDocument, startOffset)
        }

        override fun setEditor(editor: HighlighterClient) {
        }
    }

    private class PreviewHighlighterIterator(
        private val myTextModel: PreviewTextModel,
        private val myDocument: Document,
        startOffset: Int
    ) : HighlighterIterator {
        private var myCurrIndex: Int

        init {
            myCurrIndex = max(myTextModel.getIndexAtOffset(startOffset), 0)
        }

        val data: RangeHighlightingData
            get() {
                val value = myTextModel.getRangeDataAt(myCurrIndex)
                return if (value == null) EMPTY_RANGE_DATA else value
            }

        override fun getTextAttributes(): TextAttributes {
            return if (this.data.isBold) BOLD_ATTRIBUTES else PLAIN_ATTRIBUTES
        }

        override fun getStart(): Int {
            return this.data.textRange.getStartOffset()
        }

        override fun getEnd(): Int {
            return this.data.textRange.getEndOffset()
        }

        override fun getTokenType(): IElementType? {
            return null
        }

        override fun advance() {
            if (myCurrIndex < myTextModel.rangeCount - 1) myCurrIndex++
        }

        override fun retreat() {
            if (myCurrIndex > 0) myCurrIndex--
        }

        override fun atEnd(): Boolean {
            return myCurrIndex >= myTextModel.rangeCount - 1
        }

        override fun getDocument(): Document {
            return myDocument
        }

        companion object {
            private val PLAIN_ATTRIBUTES = TextAttributes(null, null, null, null, Font.PLAIN)
            private val BOLD_ATTRIBUTES = TextAttributes(null, null, null, null, Font.BOLD)

            private val EMPTY_RANGE_DATA = RangeHighlightingData(0, 0, false)
        }
    }

    companion object {
        private const val PREVIEW_TEXT_KEY = "FontPreviewText"

        private val TEXT_MODEL_KEY: Key<PreviewTextModel?> =
            Key.create<PreviewTextModel?>("RestorePreviewTextAction.textModel")

        private val TOGGLE_BOLD_SHORTCUT: ShortcutSet = CustomShortcutSet.fromString("control B")

        private val iDEDemoText: String
            get() = FontPreviewService.Companion.getInstance().fontPreviewText

        fun installTrafficLights(editor: EditorEx) {
            val markupModel = editor.getMarkupModel() as EditorMarkupModel
            markupModel.setErrorStripeRenderer(DumbTrafficLightRenderer())
            markupModel.setErrorStripeVisible(true)
        }

        fun createPreviewEditor(text: String, scheme: EditorColorsScheme, editable: Boolean): Editor {
            val editorFactory = EditorFactory.getInstance()
            val editorDocument = editorFactory.createDocument(text)
            // enable editor popup toolbar
            FileDocumentManagerBase.registerDocument(editorDocument, LightVirtualFile())
            val editor =
                (if (editable) editorFactory.createEditor(editorDocument) else editorFactory.createViewer(editorDocument)) as EditorEx
            editor.setColorsScheme(scheme)
            val settings = editor.getSettings()
            settings.setLineNumbersShown(false)
            settings.setWhitespacesShown(true)
            settings.setLineMarkerAreaShown(false)
            settings.setIndentGuidesShown(false)
            settings.setAdditionalColumnsCount(0)
            settings.setAdditionalLinesCount(0)
            settings.setRightMarginShown(true)
            settings.setRightMargin(60)
            settings.setGutterIconsShown(false)
            settings.setIndentGuidesShown(false)
            (editor.getGutter() as EditorGutterComponentEx).setPaintBackground(false)
            return editor
        }

        private fun toggleBoldFont(editor: EditorEx) {
            val textModel: PreviewTextModel? = editor.getUserData<PreviewTextModel?>(TEXT_MODEL_KEY)
            if (textModel != null) {
                val selectionModel = editor.getSelectionModel()
                textModel.toggleBoldFont(
                    TextRange.create(
                        selectionModel.getSelectionStart(),
                        selectionModel.getSelectionEnd()
                    )
                )
                editor.reinitSettings()
            }
        }
    }
}