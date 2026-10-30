package dev.nura.app.thozhil.diary.util

import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import android.provider.MediaStore
import android.os.Build
import android.net.Uri
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.data.AdjustmentType
import dev.nura.app.thozhil.diary.data.BillAdjustment
import dev.nura.app.thozhil.diary.data.BillTotals
import dev.nura.app.thozhil.diary.data.NoteItem
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.amount
import dev.nura.app.thozhil.diary.data.areaSqFt
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.data.formatSize
import dev.nura.app.thozhil.diary.ui.TableWithItems
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The looks a note or bill PDF can be made in; the choice is remembered for the next one. */
enum class PdfStyle(@param:StringRes val labelRes: Int) {
    /** Sand table headers and sage accents, like the app. */
    CLASSIC(R.string.pdf_style_classic),
    /** A dark green band across the top, green table headers and striped rows. */
    MODERN(R.string.pdf_style_modern),
    /** Black on white with spaced capital headings and ruled lines only, for plain printing. */
    MINIMAL(R.string.pdf_style_minimal)
}

/**
 * Builds an A4 PDF of a site note or bill: an "Invoice" heading with the bill's date, a panel with
 * who it's for, who prepared it and the bill total, then each table's name with its rows and total. Bills add Sq ft, Rate and
 * Amount columns and finish with a summary (items total, discount, tax, advance and the amount due)
 * and a signature line unless the bill turns it off. Tables and adjustments hidden from the PDF are left out
 * entirely, and the PDF's totals count only what it shows (see [pdfTotals]). The saved bill is not changed.
 */
object NotePdf {
    private const val PAGE_WIDTH = 595 // A4 in points
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val FOOTER_SPACE = 28f
    private const val CELL_PAD = 4f
    private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
    private const val RIGHT = PAGE_WIDTH - MARGIN

    /** The summary sits on the right under the items. */
    private const val SUMMARY_WIDTH = 250f
    private const val SUMMARY_ROW = 18f
    /** The one-row summary used when the stacked one doesn't fit the room left on a page. */
    private const val SUMMARY_STRIP_HEIGHT = 46f
    private const val GRAND_HEIGHT = 40f
    private const val SIGNATURE_HEIGHT = 56f

    private enum class Key { NUMBER, ITEM, WIDTH, LENGTH, THICKNESS, AREA, QTY, RATE, AMOUNT, NOTES }

    private class Col(val key: Key, val label: String, val width: Float, val alignEnd: Boolean = false)

    // Each set of widths adds up to the 523pt between margins
    private val noteColumns = listOf(
        Col(Key.NUMBER, "S.No.", 30f, alignEnd = true),
        Col(Key.ITEM, "Item", 120f),
        Col(Key.WIDTH, "Width", 60f, alignEnd = true),
        Col(Key.LENGTH, "Length", 60f, alignEnd = true),
        Col(Key.THICKNESS, "Thickness", 64f, alignEnd = true),
        Col(Key.QTY, "Qty", 36f, alignEnd = true),
        Col(Key.NOTES, "Note", 153f)
    )
    private val billColumns = listOf(
        Col(Key.NUMBER, "S.No.", 26f, alignEnd = true),
        Col(Key.ITEM, "Item", 86f),
        Col(Key.WIDTH, "Width", 44f, alignEnd = true),
        Col(Key.LENGTH, "Length", 44f, alignEnd = true),
        Col(Key.THICKNESS, "Thickness", 48f, alignEnd = true),
        Col(Key.AREA, "Sq ft", 40f, alignEnd = true),
        Col(Key.QTY, "Qty", 28f, alignEnd = true),
        Col(Key.RATE, "Rate / sq ft", 52f, alignEnd = true),
        Col(Key.AMOUNT, "Amount", 64f, alignEnd = true),
        Col(Key.NOTES, "Note", 91f)
    )

    /** Colours and touches for one [PdfStyle]. A null fill means that part is ruled instead of shaded. */
    private class Theme(
        val ink: Int,
        val muted: Int,
        val accent: Int,
        val rule: Int,
        val headFill: Int?,
        val headText: Int,
        val zebra: Int?,
        val totalFill: Int?,
        /** Behind the Billed to / Prepared by / Summary panel. */
        val panelFill: Int?,
        val grandFill: Int?,
        val grandLabel: Int,
        val grandValue: Int,
        /** Site name and title sit on an accent band across the top of the first page. */
        val band: Boolean,
        /** Ruled, unshaded look: table names in spaced capitals with no accent mark. */
        val plain: Boolean
    )

    private fun theme(style: PdfStyle) = when (style) {
        PdfStyle.CLASSIC -> Theme(
            ink = 0xFF1F2A22.toInt(), muted = 0xFF6B6760.toInt(), accent = 0xFF5B6E5E.toInt(),
            rule = 0xFFD9D2C7.toInt(), headFill = 0xFFE6DFD5.toInt(), headText = 0xFF5F5B55.toInt(),
            zebra = null, totalFill = 0xFFF5F1EA.toInt(), panelFill = 0xFFF7F3EC.toInt(),
            grandFill = 0xFF5B6E5E.toInt(), grandLabel = 0xFFE3EBE4.toInt(), grandValue = 0xFFFFFFFF.toInt(),
            band = false, plain = false
        )
        PdfStyle.MODERN -> Theme(
            ink = 0xFF17201B.toInt(), muted = 0xFF66706A.toInt(), accent = 0xFF1E4D3A.toInt(),
            rule = 0xFFE3E7E4.toInt(), headFill = 0xFF1E4D3A.toInt(), headText = 0xFFFFFFFF.toInt(),
            zebra = 0xFFF3F6F4.toInt(), totalFill = 0xFFE4EEE8.toInt(), panelFill = 0xFFF1F5F2.toInt(),
            grandFill = 0xFF1E4D3A.toInt(), grandLabel = 0xFFCFE0D6.toInt(), grandValue = 0xFFFFFFFF.toInt(),
            band = true, plain = false
        )
        PdfStyle.MINIMAL -> Theme(
            ink = 0xFF111111.toInt(), muted = 0xFF666666.toInt(), accent = 0xFF111111.toInt(),
            rule = 0xFFCCCCCC.toInt(), headFill = null, headText = 0xFF111111.toInt(),
            zebra = null, totalFill = null, panelFill = null,
            grandFill = null, grandLabel = 0xFF111111.toInt(), grandValue = 0xFF111111.toInt(),
            band = false, plain = true
        )
    }

    private fun paint(size: Float, color: Int, face: Typeface) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = face
    }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }

    private class Paints(t: Theme) {
        private val sans = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        private val sansBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        private val onBand = 0xFFFFFFFF.toInt()
        private val onBandMuted = 0xFFCFE0D6.toInt()

        val title = paint(22f, if (t.band) onBand else t.ink, sansBold)
        val subtitle = paint(12.5f, if (t.band) onBandMuted else t.accent, sansBold)
        val docLabel = paint(10f, if (t.band) onBand else t.accent, sansBold).apply { letterSpacing = 0.2f }
        val date = paint(10f, if (t.band) onBandMuted else t.muted, sans)
        val muted = paint(10f, t.muted, sans)
        /** Small spaced capitals over a value, e.g. BILLED TO. */
        val label = paint(7.5f, t.muted, sansBold).apply { letterSpacing = 0.14f }
        val metaStrong = paint(10.5f, t.ink, sansBold)
        /** The bill total in the panel's Summary column. */
        val summaryValueStrong = paint(14f, t.accent, sansBold)
        val meta = paint(9.5f, t.ink, sans)
        val small = paint(8f, t.muted, sans)
        val tableName = if (t.plain) {
            paint(10.5f, t.ink, sansBold).apply { letterSpacing = 0.12f }
        } else {
            paint(12.5f, t.ink, sansBold)
        }
        val tableMeta = paint(8.5f, t.muted, sans)
        val head = paint(7.5f, t.headText, sansBold)
        val cell = paint(8.5f, t.ink, sans)
        val cellMuted = paint(8.5f, t.muted, sans)
        val cellBold = paint(8.5f, t.ink, sansBold)
        val total = paint(8.5f, t.accent, sansBold)
        val summaryLabel = paint(9.5f, t.muted, sans)
        val summaryValue = paint(9.5f, t.ink, sans)
        val summaryStrong = paint(10.5f, t.ink, sansBold)
        val grandLabel = paint(11f, t.grandLabel, sansBold)
        val grandValue = paint(16f, t.grandValue, sansBold)
        /** "BALANCE DUE" over the amount in the one-row summary's box. */
        val grandCaption = paint(7.5f, t.grandLabel, sansBold).apply { letterSpacing = 0.14f }
        val footer = paint(7.5f, t.muted, sans)
        val rule = Paint().apply { color = t.rule; strokeWidth = 0.8f }
        val strongRule = Paint().apply { color = t.ink; strokeWidth = 1.2f }
        val accentFill = fill(t.accent)
        val headFill = t.headFill?.let(::fill)
        val zebraFill = t.zebra?.let(::fill)
        val totalFill = t.totalFill?.let(::fill)
        val panelFill = t.panelFill?.let(::fill)
        val grandFill = t.grandFill?.let(::fill)
    }

    private fun layout(text: String, paint: TextPaint, width: Float, alignEnd: Boolean = false): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(if (alignEnd) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL)
            .build()

    /**
     * Tracks the current page and the pen's vertical position, starting new pages as needed. The footer
     * says "Page 2 of 3" once [totalPages] is known (see [write]).
     */
    private class Writer(
        private val doc: PdfDocument,
        private val p: Paints,
        private val footerLeft: String,
        private val totalPages: Int?
    ) {
        var pageNumber = 0
        lateinit var page: PdfDocument.Page
        val canvas: Canvas get() = page.canvas
        var y = MARGIN

        fun newPage() {
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
            y = MARGIN
        }

        fun finishPage() {
            val footerY = PAGE_HEIGHT - MARGIN + 8f
            canvas.drawLine(MARGIN, footerY - 12f, RIGHT, footerY - 12f, p.rule)
            val label = if (totalPages != null) "Page $pageNumber of $totalPages" else "Page $pageNumber"
            val labelWidth = p.footer.measureText(label)
            val left = TextUtils.ellipsize(footerLeft, p.footer, CONTENT_WIDTH - labelWidth - 24f, TextUtils.TruncateAt.END)
            canvas.drawText(left, 0, left.length, MARGIN, footerY, p.footer)
            canvas.drawText(label, RIGHT - labelWidth, footerY, p.footer)
            doc.finishPage(page)
        }

        /** Room left on this page above the footer. */
        fun spaceLeft(): Float = PAGE_HEIGHT - MARGIN - FOOTER_SPACE - y

        /** Starts a new page when [height] doesn't fit; returns true if it did. */
        fun ensureSpace(height: Float): Boolean {
            if (height <= spaceLeft()) return false
            finishPage()
            newPage()
            return true
        }

        fun draw(layout: StaticLayout, x: Float, top: Float = y) {
            canvas.save()
            canvas.translate(x, top)
            layout.draw(canvas)
            canvas.restore()
        }
    }

    fun write(
        context: Context,
        site: SiteWithContacts?,
        note: SiteNote,
        tables: List<TableWithItems>,
        adjustments: List<BillAdjustment> = emptyList(),
        style: PdfStyle = PdfStyle.CLASSIC,
        /** The logged-in user's name, shown as having prepared it. */
        preparedBy: String = ""
    ): File {
        // A first pass only counts the pages, so every footer can say "Page 1 of 3"
        val pages = PdfDocument().let { probe ->
            render(probe, style, site, note, tables, adjustments, preparedBy, totalPages = null).also { probe.close() }
        }
        val doc = PdfDocument()
        render(doc, style, site, note, tables, adjustments, preparedBy, totalPages = pages)

        val dir = File(context.cacheDir, "pdfs").apply {
            mkdirs()
            listFiles()?.forEach { it.delete() } // only the latest export is kept
        }
        val safe = { s: String -> s.replace(Regex("[^A-Za-z0-9 _-]"), "").trim().ifEmpty { "Note" } }
        val file = File(dir, "${safe(site?.site?.name ?: "Site")} - ${safe(note.title)} - ${LocalDate.now()}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /** Draws the whole document into [doc] and returns how many pages it took. */
    private fun render(
        doc: PdfDocument,
        style: PdfStyle,
        site: SiteWithContacts?,
        note: SiteNote,
        tables: List<TableWithItems>,
        adjustments: List<BillAdjustment>,
        preparedBy: String,
        totalPages: Int?
    ): Int {
        val t = theme(style)
        val p = Paints(t)
        val columns = if (note.isBill) billColumns else noteColumns
        val shownTables = tables.filterNot { it.table.hideInPdf }
        val shownAdjustments = adjustments.filterNot { it.hideInPdf }
        val totals = if (note.isBill) pdfTotals(tables, adjustments) else null
        val w = Writer(doc, p, "Thozhil Diary · ${site?.site?.name ?: "Site"} · ${note.title}", totalPages)
        w.newPage()

        drawHeading(w, t, p, site, note)
        drawInfoPanel(w, p, site, note, preparedBy, totals)

        if (shownTables.isEmpty()) {
            val message = if (tables.isEmpty()) "This ${if (note.isBill) "bill" else "note"} has no tables yet." else "No tables to show."
            w.draw(layout(message, p.muted, CONTENT_WIDTH), MARGIN)
            w.y += p.muted.textSize + 20f
        }
        shownTables.forEach { drawTable(w, t, p, columns, it) }

        if (totals != null && (tables.isNotEmpty() || adjustments.isNotEmpty())) {
            drawSummary(w, p, totals, shownAdjustments, withSignature = note.showSignature)
            if (note.showSignature) drawSignature(w, p)
        }

        w.finishPage()
        return w.pageNumber
    }

    /**
     * "Invoice" (a note: its site name) and the title on the left; the date it was made on the right.
     * A bill's site name is under Billed to in the panel below.
     */
    private fun drawHeading(w: Writer, t: Theme, p: Paints, site: SiteWithContacts?, note: SiteNote) {
        val date = Instant.ofEpochMilli(note.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
            .format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        val docLabel = if (note.isBill) "DATE" else "SITE NOTE"
        val rightWidth = maxOf(p.date.measureText(date), p.docLabel.measureText(docLabel))
        val textWidth = CONTENT_WIDTH - rightWidth - 16f
        val heading = if (note.isBill) "Invoice" else site?.site?.name ?: "Site"
        val siteLayout = layout(heading, p.title, textWidth)
        val titleLayout = layout(note.title, p.subtitle, textWidth)
        val top = if (t.band) MARGIN + 4f else MARGIN
        val textBottom = top + siteLayout.height + 4f + titleLayout.height

        if (t.band) w.canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), textBottom + 22f, p.accentFill)
        w.draw(siteLayout, MARGIN, top)
        w.draw(titleLayout, MARGIN, top + siteLayout.height + 4f)
        val labelBaseline = top + p.docLabel.textSize + 4f
        w.canvas.drawText(docLabel, RIGHT - p.docLabel.measureText(docLabel), labelBaseline, p.docLabel)
        w.canvas.drawText(date, RIGHT - p.date.measureText(date), labelBaseline + p.date.textSize + 6f, p.date)
        w.y = textBottom + if (t.band) 22f + 16f else 14f
        if (!t.band) {
            w.canvas.drawLine(MARGIN, w.y, RIGHT, w.y, if (t.plain) p.strongRule else p.rule)
            w.y += 14f
        }
    }

    /**
     * Who the bill is for and who prepared it, then for a bill a Summary column with its total
     * (as the PDF counts it). A note has no total, so it gets the first two columns only.
     */
    private fun drawInfoPanel(
        w: Writer,
        p: Paints,
        site: SiteWithContacts?,
        note: SiteNote,
        preparedBy: String,
        totals: BillTotals?
    ) {
        val pad = 12f
        val gap = 16f
        val contact = site?.contacts?.firstOrNull()
        val content = buildList {
            add((if (note.isBill) "BILLED TO" else "SITE") to buildList {
                add((site?.site?.name ?: "Site") to p.metaStrong)
                contact?.let {
                    add(it.name to p.meta)
                    if (it.mobile.isNotEmpty()) add(it.mobile to p.meta)
                }
            })
            add("PREPARED BY" to listOf(preparedBy.ifBlank { "–" } to p.metaStrong))
            totals?.let { add("SUMMARY" to listOf("Bill total" to p.meta, formatRupees(it.billTotal) to p.summaryValueStrong)) }
        }
        val colWidth = (CONTENT_WIDTH - 2 * pad - gap * (content.size - 1)) / content.size
        val columns = content.map { (label, lines) ->
            layout(label, p.label, colWidth) to lines.map { (text, paint) -> layout(text, paint, colWidth) }
        }
        val height = columns.maxOf { (label, lines) -> label.height + 4f + lines.sumOf { it.height.toDouble() }.toFloat() + 2f * (lines.size - 1) }
        val boxHeight = height + 2 * pad
        val top = w.y
        val panel = p.panelFill
        if (panel != null) {
            w.canvas.drawRoundRect(MARGIN, top, RIGHT, top + boxHeight, 6f, 6f, panel)
        } else {
            w.canvas.drawLine(MARGIN, top + boxHeight, RIGHT, top + boxHeight, p.rule)
        }
        columns.forEachIndexed { i, (label, lines) ->
            val x = MARGIN + pad + i * (colWidth + gap)
            var y = top + pad
            w.draw(label, x, y)
            y += label.height + 4f
            lines.forEach {
                w.draw(it, x, y)
                y += it.height + 2f
            }
        }
        w.y = top + boxHeight + 22f
    }

    /** The table's name with an accent mark (spaced capitals in Minimal), marked "continued" on later pages. */
    private fun drawTableName(w: Writer, t: Theme, p: Paints, name: String, continued: Boolean = false) {
        val indent = if (t.plain) 0f else 10f
        val metaText = if (continued) "continued" else ""
        val metaWidth = p.tableMeta.measureText(metaText)
        val nameLayout = layout(if (t.plain) name.uppercase() else name, p.tableName, CONTENT_WIDTH - indent - metaWidth - 16f)
        if (!t.plain) w.canvas.drawRect(MARGIN, w.y + 2f, MARGIN + 3.5f, w.y + nameLayout.height - 2f, p.accentFill)
        w.draw(nameLayout, MARGIN + indent)
        if (continued) w.canvas.drawText(metaText, RIGHT - metaWidth, w.y + nameLayout.getLineBaseline(0).toFloat(), p.tableMeta)
        w.y += nameLayout.height
    }

    /**
     * A bill's totals as its PDF shows them: only tables and adjustments not hidden from the PDF count.
     */
    fun pdfTotals(tables: List<TableWithItems>, adjustments: List<BillAdjustment>): BillTotals = BillTotals.of(
        tables.filterNot { it.table.hideInPdf }.sumOf { e -> e.items.sumOf { it.amount() ?: 0.0 } },
        adjustments.filterNot { it.hideInPdf }
    )

    /** Sq ft of all pieces in [items]: each row's area × its quantity. */
    private fun totalArea(items: List<NoteItem>) = items.sumOf { (it.areaSqFt() ?: 0.0) * (it.quantity ?: 1) }

    /** Name first, then the header, rows and total. */
    private fun drawTable(w: Writer, t: Theme, p: Paints, columns: List<Col>, entry: TableWithItems) {
        val (table, items) = entry
        val pieces = items.sumOf { it.quantity ?: 0 }
        val amount = items.sumOf { it.amount() ?: 0.0 }
        val area = totalArea(items)

        // The name stays with the header row and at least one line
        w.ensureSpace(p.tableName.textSize + 80f)
        drawTableName(w, t, p, table.name)
        w.y += 6f
        if (items.isEmpty()) {
            w.draw(layout("No items", p.muted, CONTENT_WIDTH), MARGIN)
            w.y += p.muted.textSize + 24f
            return
        }

        val totalHeight = p.cell.textSize + 2 * CELL_PAD + 8f
        drawHeaderRow(w, p, columns)
        items.forEachIndexed { index, item ->
            val layouts = columns.map { col ->
                val paint = when {
                    col.key == Key.ITEM -> p.cellBold
                    col.key == Key.NUMBER || col.key == Key.NOTES || (col.key == Key.AREA && item.areaSqFt() == null) -> p.cellMuted
                    else -> p.cell
                }
                layout(cellFor(col.key, index, item), paint, col.width - 2 * CELL_PAD, col.alignEnd)
            }
            val rowHeight = layouts.maxOf { it.height } + 2 * CELL_PAD + 4f
            // On a new page the table's name and header come again; the last row keeps its total
            // with it, so a total never starts a page on its own
            val needed = if (index == items.lastIndex) rowHeight + totalHeight else rowHeight
            if (w.ensureSpace(needed)) {
                drawTableName(w, t, p, table.name, continued = true)
                w.y += 6f
                drawHeaderRow(w, p, columns)
            }
            if (index % 2 == 1) p.zebraFill?.let { w.canvas.drawRect(MARGIN, w.y, RIGHT, w.y + rowHeight, it) }
            var x = MARGIN
            layouts.forEachIndexed { i, l ->
                w.draw(l, x + CELL_PAD, w.y + CELL_PAD + 2f)
                x += columns[i].width
            }
            w.y += rowHeight
            w.canvas.drawLine(MARGIN, w.y, RIGHT, w.y, p.rule)
        }

        p.totalFill?.let { w.canvas.drawRect(MARGIN, w.y, RIGHT, w.y + totalHeight, it) }
        if (p.totalFill == null) w.canvas.drawLine(MARGIN, w.y, RIGHT, w.y, p.strongRule)
        var x = MARGIN
        columns.forEach { col ->
            val text = when (col.key) {
                Key.ITEM -> "Total"
                Key.QTY -> "$pieces"
                Key.AREA -> if (area > 0) formatSqFt(area) else ""
                Key.AMOUNT -> formatRupees(amount)
                else -> ""
            }
            val paint = if (col.key == Key.ITEM) p.cellBold else p.total
            w.draw(layout(text, paint, col.width - 2 * CELL_PAD, col.alignEnd), x + CELL_PAD, w.y + CELL_PAD + 4f)
            x += col.width
        }
        w.y += totalHeight + 26f
    }

    /** Column labels; long ones such as "Rate / sq ft" wrap, and the row grows to fit. */
    private fun drawHeaderRow(w: Writer, p: Paints, columns: List<Col>) {
        val labels = columns.map { layout(it.label, p.head, it.width - 2 * CELL_PAD, it.alignEnd) }
        val height = labels.maxOf { it.height } + 2 * CELL_PAD + 6f
        p.headFill?.let { w.canvas.drawRect(MARGIN, w.y, RIGHT, w.y + height, it) }
        var x = MARGIN
        labels.forEachIndexed { i, l ->
            w.draw(l, x + CELL_PAD, w.y + CELL_PAD + 3f)
            x += columns[i].width
        }
        w.y += height
        if (p.headFill == null) w.canvas.drawLine(MARGIN, w.y, RIGHT, w.y, p.strongRule)
    }

    private class SummaryRow(val label: String, val value: String, val strong: Boolean = false)

    /**
     * On the right: items total, each discount and tax, the bill total, each advance, and the amount
     * due in a highlighted box. When that stack doesn't fit the room left on the page, the totals sit
     * side by side in one row instead ([drawSummaryRow]); only if that doesn't fit either does the
     * summary move to the next page, whole rather than split.
     */
    private fun drawSummary(
        w: Writer,
        p: Paints,
        totals: BillTotals,
        adjustments: List<BillAdjustment>,
        withSignature: Boolean
    ) {
        val priced = adjustments.filter { it.type != AdjustmentType.ADVANCE }
        val advances = adjustments.filter { it.type == AdjustmentType.ADVANCE }
        val rows = buildList {
            add(SummaryRow("Items total", formatRupees(totals.subtotal)))
            priced.forEach {
                val sign = if (it.type == AdjustmentType.DISCOUNT) "− " else "+ "
                add(SummaryRow(it.type.label, sign + formatRupees(it.amount)))
            }
            // The bill total gets its own line only when the box below shows the balance instead
            if (priced.isNotEmpty() && advances.isNotEmpty()) add(SummaryRow("Bill total", formatRupees(totals.billTotal), strong = true))
            advances.forEach { add(SummaryRow(it.type.label, "− " + formatRupees(it.amount))) }
        }
        val (grandLabel, grandAmount) =
            if (advances.isNotEmpty()) "Balance due" to totals.balanceDue else "Bill total" to totals.billTotal

        val left = RIGHT - SUMMARY_WIDTH
        val height = rows.size * SUMMARY_ROW + 8f + GRAND_HEIGHT
        // Room for the signature too, so it never lands alone on a page of its own
        val signature = if (withSignature) SIGNATURE_HEIGHT else 0f
        if (height + 12f + signature > w.spaceLeft() && SUMMARY_STRIP_HEIGHT + 12f + signature <= w.spaceLeft()) {
            drawSummaryRow(w, p, totals, grandLabel, grandAmount)
            return
        }
        w.ensureSpace(height + 12f + signature)

        val top = w.y
        var y = top
        rows.forEach { row ->
            if (row.strong) w.canvas.drawLine(left, y, RIGHT, y, p.rule)
            val labelPaint = if (row.strong) p.summaryStrong else p.summaryLabel
            val valuePaint = if (row.strong) p.summaryStrong else p.summaryValue
            val baseline = y + SUMMARY_ROW - 5f
            w.canvas.drawText(row.label, left + 8f, baseline, labelPaint)
            w.canvas.drawText(row.value, RIGHT - 8f - valuePaint.measureText(row.value), baseline, valuePaint)
            y += SUMMARY_ROW
        }
        y += 8f
        val box = p.grandFill
        if (box != null) {
            w.canvas.drawRoundRect(left, y, RIGHT, y + GRAND_HEIGHT, 6f, 6f, box)
        } else {
            w.canvas.drawLine(left, y, RIGHT, y, p.strongRule)
            w.canvas.drawLine(left, y + 3f, RIGHT, y + 3f, p.strongRule)
        }
        val value = formatRupees(grandAmount)
        val baseline = y + GRAND_HEIGHT / 2 + p.grandValue.textSize * 0.36f
        w.canvas.drawText(grandLabel, left + 12f, baseline - 1f, p.grandLabel)
        w.canvas.drawText(value, RIGHT - 12f - p.grandValue.measureText(value), baseline, p.grandValue)

        w.y = top + height + 12f
    }

    /**
     * The summary in one row across the page: Items total, Advance, Tax and Discount (each type added
     * up), the bill total when the box shows the balance instead, then the amount due in the box.
     */
    private fun drawSummaryRow(w: Writer, p: Paints, totals: BillTotals, grandLabel: String, grandAmount: Double) {
        val cells = buildList {
            add("ITEMS TOTAL" to formatRupees(totals.subtotal))
            if (totals.advance != 0.0) add("ADVANCE" to "− " + formatRupees(totals.advance))
            if (totals.tax != 0.0) add("TAX" to "+ " + formatRupees(totals.tax))
            if (totals.discount != 0.0) add("DISCOUNT" to "− " + formatRupees(totals.discount))
            if (totals.advance != 0.0 && (totals.tax != 0.0 || totals.discount != 0.0)) {
                add("BILL TOTAL" to formatRupees(totals.billTotal))
            }
        }
        val height = SUMMARY_STRIP_HEIGHT
        val boxWidth = 150f
        val cellsRight = RIGHT - boxWidth - 8f
        val cellWidth = (cellsRight - MARGIN) / cells.size
        val top = w.y

        val panel = p.panelFill
        if (panel != null) {
            w.canvas.drawRoundRect(MARGIN, top, cellsRight, top + height, 6f, 6f, panel)
        } else {
            w.canvas.drawLine(MARGIN, top, cellsRight, top, p.rule)
            w.canvas.drawLine(MARGIN, top + height, cellsRight, top + height, p.rule)
        }
        cells.forEachIndexed { i, (label, value) ->
            val x = MARGIN + i * cellWidth
            if (i > 0) w.canvas.drawLine(x, top + 8f, x, top + height - 8f, p.rule)
            val shown = TextUtils.ellipsize(value, p.summaryStrong, cellWidth - 20f, TextUtils.TruncateAt.END)
            w.canvas.drawText(label, x + 10f, top + 16f, p.label)
            w.canvas.drawText(shown, 0, shown.length, x + 10f, top + 34f, p.summaryStrong)
        }

        val boxLeft = RIGHT - boxWidth
        val box = p.grandFill
        if (box != null) {
            w.canvas.drawRoundRect(boxLeft, top, RIGHT, top + height, 6f, 6f, box)
        } else {
            w.canvas.drawLine(boxLeft, top, RIGHT, top, p.strongRule)
            w.canvas.drawLine(boxLeft, top + 3f, RIGHT, top + 3f, p.strongRule)
        }
        w.canvas.drawText(grandLabel.uppercase(), boxLeft + 12f, top + 16f, p.grandCaption)
        val value = formatRupees(grandAmount)
        w.canvas.drawText(value, RIGHT - 12f - p.grandValue.measureText(value), top + 37f, p.grandValue)

        w.y = top + height + 12f
    }

    /** A line to sign on, on the right under the summary. */
    private fun drawSignature(w: Writer, p: Paints) {
        w.ensureSpace(SIGNATURE_HEIGHT)
        w.y += SIGNATURE_HEIGHT - 20f
        val left = RIGHT - 170f
        w.canvas.drawLine(left, w.y, RIGHT, w.y, p.strongRule)
        val label = "Authorised signature"
        w.canvas.drawText(label, RIGHT - p.small.measureText(label), w.y + 12f, p.small)
        w.y += 20f
    }

    private fun cellFor(key: Key, index: Int, item: NoteItem): String = when (key) {
        Key.NUMBER -> "${index + 1}"
        Key.ITEM -> item.item
        Key.THICKNESS -> formatSize(item.thickness, item.thicknessUnit)
        Key.WIDTH -> formatSize(item.width, item.widthUnit)
        Key.LENGTH -> formatSize(item.length, item.lengthUnit)
        Key.AREA -> item.areaSqFt()?.let(::formatSqFt) ?: if (item.rate != null) "per pc" else "–"
        Key.QTY -> item.quantity?.toString() ?: "–"
        Key.RATE -> item.rate?.let(::formatRupees) ?: "–"
        Key.AMOUNT -> item.amount()?.let(::formatRupees) ?: "–"
        Key.NOTES -> item.remarks.ifEmpty { "–" }
    }
}

/** Saves a copy of [file] in the public Downloads folder (Android 10 and up, no permission needed). */
@RequiresApi(Build.VERSION_CODES.Q)
fun Context.savePdfToDownloads(file: File): Uri {
    val values = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, file.name)
        put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
        put(MediaStore.Downloads.IS_PENDING, 1)
    }
    val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        ?: error("Downloads is not available")
    copyPdfTo(file, uri)
    values.clear()
    values.put(MediaStore.Downloads.IS_PENDING, 0)
    contentResolver.update(uri, values, null, null)
    return uri
}

/** Writes [file] into a place the person picked, such as from the system "save as" screen. */
fun Context.copyPdfTo(file: File, uri: Uri) {
    val out = contentResolver.openOutputStream(uri) ?: error("Can't write to $uri")
    out.use { stream -> file.inputStream().use { it.copyTo(stream) } }
}

/** Opens the share sheet for a PDF made by [NotePdf], so it can be sent or saved. */
fun Context.sharePdf(file: File) {
    val uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(send, "Share PDF").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
