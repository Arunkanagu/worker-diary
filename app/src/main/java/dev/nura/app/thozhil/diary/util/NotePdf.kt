package dev.nura.app.thozhil.diary.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import dev.nura.app.thozhil.diary.data.NoteItem
import dev.nura.app.thozhil.diary.data.SiteNote
import dev.nura.app.thozhil.diary.data.amount
import dev.nura.app.thozhil.diary.data.SiteWithContacts
import dev.nura.app.thozhil.diary.data.formatSize
import dev.nura.app.thozhil.diary.ui.TableWithItems
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Builds an A4 PDF of a site note or bill: site, first contact, date, then every table with totals.
 * Bills add Rate and Amount columns and a grand total.
 */
object NotePdf {
    private const val PAGE_WIDTH = 595 // A4 in points
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f
    private const val FOOTER_SPACE = 28f
    private const val CELL_PAD = 4f

    private const val INK = 0xFF1F2A22.toInt()
    private const val MUTED = 0xFF6B6760.toInt()
    private const val SAGE = 0xFF5B6E5E.toInt()
    private const val HEADER_FILL = 0xFFE6DFD5.toInt()
    private const val RULE = 0xFFD9D2C7.toInt()

    private enum class Key { NUMBER, ITEM, THICKNESS, WIDTH, LENGTH, QTY, RATE, AMOUNT, NOTES }

    private class Col(val key: Key, val label: String, val width: Float, val alignEnd: Boolean = false)

    // Each set of widths adds up to the 523pt between margins
    private val noteColumns = listOf(
        Col(Key.NUMBER, "#", 22f, alignEnd = true),
        Col(Key.ITEM, "Item", 122f),
        Col(Key.THICKNESS, "Thickness", 64f, alignEnd = true),
        Col(Key.WIDTH, "Width", 60f, alignEnd = true),
        Col(Key.LENGTH, "Length", 60f, alignEnd = true),
        Col(Key.QTY, "Qty", 40f, alignEnd = true),
        Col(Key.NOTES, "Notes", 155f)
    )
    private val billColumns = listOf(
        Col(Key.NUMBER, "#", 20f, alignEnd = true),
        Col(Key.ITEM, "Item", 104f),
        Col(Key.THICKNESS, "Thick.", 50f, alignEnd = true),
        Col(Key.WIDTH, "Width", 50f, alignEnd = true),
        Col(Key.LENGTH, "Length", 50f, alignEnd = true),
        Col(Key.QTY, "Qty", 30f, alignEnd = true),
        Col(Key.RATE, "Rate", 64f, alignEnd = true),
        Col(Key.AMOUNT, "Amount", 74f, alignEnd = true),
        Col(Key.NOTES, "Notes", 81f)
    )

    private fun paint(size: Float, color: Int, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private val titlePaint = paint(20f, INK, bold = true)
    private val subtitlePaint = paint(13f, SAGE, bold = true)
    private val bodyPaint = paint(10.5f, INK)
    private val mutedPaint = paint(10f, MUTED)
    private val tableNamePaint = paint(12.5f, INK, bold = true)
    private val headPaint = paint(9f, MUTED, bold = true)
    private val cellPaint = paint(9.5f, INK)
    private val cellBoldPaint = paint(9.5f, INK, bold = true)
    private val totalPaint = paint(9.5f, SAGE, bold = true)
    private val footerPaint = paint(8f, MUTED)
    private val rulePaint = Paint().apply { color = RULE; strokeWidth = 0.8f }
    private val fillPaint = Paint().apply { color = HEADER_FILL; style = Paint.Style.FILL }

    private fun layout(text: String, paint: TextPaint, width: Float, alignEnd: Boolean = false): StaticLayout =
        StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt().coerceAtLeast(1))
            .setAlignment(if (alignEnd) Layout.Alignment.ALIGN_OPPOSITE else Layout.Alignment.ALIGN_NORMAL)
            .build()

    /** Tracks the current page and the pen's vertical position, starting new pages as needed. */
    private class Writer(private val doc: PdfDocument) {
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
            canvas.drawText("Thozhil Diary", MARGIN, footerY, footerPaint)
            val label = "Page $pageNumber"
            canvas.drawText(label, PAGE_WIDTH - MARGIN - footerPaint.measureText(label), footerY, footerPaint)
            doc.finishPage(page)
        }

        /** Starts a new page when [height] doesn't fit; returns true if it did. */
        fun ensureSpace(height: Float): Boolean {
            if (y + height <= PAGE_HEIGHT - MARGIN - FOOTER_SPACE) return false
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

    fun write(context: Context, site: SiteWithContacts?, note: SiteNote, tables: List<TableWithItems>): File {
        val contentWidth = PAGE_WIDTH - 2 * MARGIN
        val columns = if (note.isBill) billColumns else noteColumns
        val doc = PdfDocument()
        val w = Writer(doc)
        w.newPage()

        // Heading: site name with the date on the right, note title, first contact
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        val dateWidth = mutedPaint.measureText(date)
        val siteLayout = layout(site?.site?.name ?: "Site", titlePaint, contentWidth - dateWidth - 16f)
        w.draw(siteLayout, MARGIN)
        w.canvas.drawText(date, PAGE_WIDTH - MARGIN - dateWidth, w.y + mutedPaint.textSize + 4f, mutedPaint)
        w.y += siteLayout.height + 6f
        val titleLayout = layout(note.title, subtitlePaint, contentWidth)
        w.draw(titleLayout, MARGIN)
        w.y += titleLayout.height + 6f
        site?.contacts?.firstOrNull()?.let { contact ->
            val line = if (contact.mobile.isEmpty()) "Contact: ${contact.name}" else "Contact: ${contact.name}, ${contact.mobile}"
            val contactLayout = layout(line, bodyPaint, contentWidth)
            w.draw(contactLayout, MARGIN)
            w.y += contactLayout.height
        }
        w.y += 10f
        w.canvas.drawLine(MARGIN, w.y, PAGE_WIDTH - MARGIN, w.y, rulePaint)
        w.y += 18f

        if (tables.isEmpty()) {
            w.draw(layout("This note has no tables yet.", mutedPaint, contentWidth), MARGIN)
        }

        tables.forEach { (table, items) ->
            val pieces = items.sumOf { it.quantity ?: 0 }
            val amount = items.sumOf { it.amount() ?: 0.0 }
            // Keep the table name with its header row and at least one line
            w.ensureSpace(70f)
            val nameLayout = layout(table.name, tableNamePaint, contentWidth)
            w.draw(nameLayout, MARGIN)
            w.y += nameLayout.height + 2f
            val summary = when {
                items.isEmpty() -> "No items"
                note.isBill -> "${items.size} ${if (items.size == 1) "item" else "items"}, ${formatRupees(amount)}"
                else -> "${items.size} ${if (items.size == 1) "item" else "items"}, $pieces pcs"
            }
            val summaryLayout = layout(summary, mutedPaint, contentWidth)
            w.draw(summaryLayout, MARGIN)
            w.y += summaryLayout.height + 6f

            if (items.isNotEmpty()) {
                drawHeaderRow(w, columns)
                items.forEachIndexed { index, item ->
                    val layouts = columns.map { col ->
                        layout(cellFor(col.key, index, item), if (col.key == Key.ITEM) cellBoldPaint else cellPaint, col.width - 2 * CELL_PAD, col.alignEnd)
                    }
                    val rowHeight = layouts.maxOf { it.height } + 2 * CELL_PAD + 2f
                    if (w.ensureSpace(rowHeight)) drawHeaderRow(w, columns)
                    drawRow(w, columns, layouts, rowHeight)
                    w.canvas.drawLine(MARGIN, w.y, PAGE_WIDTH - MARGIN, w.y, rulePaint)
                }
                val totalHeight = cellPaint.textSize + 2 * CELL_PAD + 6f
                w.ensureSpace(totalHeight)
                var x = MARGIN
                val totals = columns.map { col ->
                    val text = when (col.key) {
                        Key.ITEM -> "Total"
                        Key.QTY -> "$pieces"
                        Key.AMOUNT -> formatRupees(amount)
                        else -> ""
                    }
                    val paint = if (col.key == Key.QTY || col.key == Key.AMOUNT) totalPaint else cellBoldPaint
                    layout(text, paint, col.width - 2 * CELL_PAD, col.alignEnd)
                }
                totals.forEachIndexed { i, l ->
                    w.draw(l, x + CELL_PAD, w.y + CELL_PAD + 2f)
                    x += columns[i].width
                }
                w.y += totalHeight
            }
            w.y += 20f
        }

        if (note.isBill && tables.isNotEmpty()) {
            val grand = tables.sumOf { t -> t.items.sumOf { it.amount() ?: 0.0 } }
            w.ensureSpace(40f)
            w.canvas.drawLine(MARGIN, w.y, PAGE_WIDTH - MARGIN, w.y, rulePaint)
            w.y += 10f
            val label = "Bill total"
            val value = formatRupees(grand)
            val baseline = w.y + titlePaint.textSize * 0.8f
            w.canvas.drawText(label, MARGIN, baseline, subtitlePaint)
            w.canvas.drawText(value, PAGE_WIDTH - MARGIN - titlePaint.measureText(value), baseline, titlePaint)
            w.y += titlePaint.textSize + 8f
        }

        w.finishPage()

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

    private fun cellFor(key: Key, index: Int, item: NoteItem): String = when (key) {
        Key.NUMBER -> "${index + 1}"
        Key.ITEM -> item.item
        Key.THICKNESS -> formatSize(item.thickness, item.thicknessUnit)
        Key.WIDTH -> formatSize(item.width, item.widthUnit)
        Key.LENGTH -> formatSize(item.length, item.lengthUnit)
        Key.QTY -> item.quantity?.toString() ?: "–"
        Key.RATE -> item.rate?.let(::formatRupees) ?: "–"
        Key.AMOUNT -> item.amount()?.let(::formatRupees) ?: "–"
        Key.NOTES -> item.remarks.ifEmpty { "–" }
    }

    private fun drawHeaderRow(w: Writer, columns: List<Col>) {
        val height = headPaint.textSize + 2 * CELL_PAD + 6f
        w.canvas.drawRect(MARGIN, w.y, PAGE_WIDTH - MARGIN, w.y + height, fillPaint)
        var x = MARGIN
        columns.forEach { col ->
            w.draw(layout(col.label, headPaint, col.width - 2 * CELL_PAD, col.alignEnd), x + CELL_PAD, w.y + CELL_PAD + 2f)
            x += col.width
        }
        w.y += height
    }

    private fun drawRow(w: Writer, columns: List<Col>, layouts: List<StaticLayout>, height: Float) {
        var x = MARGIN
        layouts.forEachIndexed { i, l ->
            w.draw(l, x + CELL_PAD, w.y + CELL_PAD + 1f)
            x += columns[i].width
        }
        w.y += height
    }
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
