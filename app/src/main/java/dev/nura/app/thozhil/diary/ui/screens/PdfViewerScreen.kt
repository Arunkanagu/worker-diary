package dev.nura.app.thozhil.diary.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nura.app.thozhil.diary.R
import dev.nura.app.thozhil.diary.ui.NoteViewModel
import dev.nura.app.thozhil.diary.ui.components.CalmSegmented
import dev.nura.app.thozhil.diary.ui.components.ScreenHeader
import dev.nura.app.thozhil.diary.ui.theme.Cover
import dev.nura.app.thozhil.diary.ui.theme.InkMuted
import dev.nura.app.thozhil.diary.ui.theme.OnCoverMuted
import dev.nura.app.thozhil.diary.ui.theme.SageDeep
import dev.nura.app.thozhil.diary.ui.theme.SandLight
import dev.nura.app.thozhil.diary.util.NotePdf
import dev.nura.app.thozhil.diary.util.PdfStyle
import dev.nura.app.thozhil.diary.util.SharedPrefHelper
import dev.nura.app.thozhil.diary.util.copyPdfTo
import dev.nura.app.thozhil.diary.util.savePdfToDownloads
import dev.nura.app.thozhil.diary.util.sharePdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

/** Pages are rendered at twice their on-screen width, up to this, so text stays sharp at 2x zoom. */
private const val MAX_PAGE_PX = 2000
private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 4f
/** The steps the − and + buttons move through. */
private val ZOOM_STEPS = listOf(1f, 1.5f, 2f, 3f, 4f)
/** Widest a page is shown at 100%, so it reads like paper on a tablet. */
private val PAGE_MAX_WIDTH = 720.dp
private val PAGE_GUTTER = 20.dp

/**
 * Shows a generated PDF page by page so it can be checked before it goes anywhere,
 * with Share and Download underneath. Pinch, double-tap or the − / + control to zoom.
 * The style switch at the top remakes the PDF from the note in another look and remembers it.
 */
@Composable
fun PdfViewerScreen(
    path: String,
    onBack: () -> Unit,
    viewModel: NoteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var file by remember(path) { mutableStateOf(File(path)) }
    val density = LocalDensity.current
    val prefs = remember { SharedPrefHelper(context) }

    val note by viewModel.note.collectAsStateWithLifecycle()
    val site by viewModel.site.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val adjustments by viewModel.adjustments.collectAsStateWithLifecycle()

    var style by remember { mutableStateOf(prefs.getPdfStyle()) }
    var pages by remember { mutableStateOf<List<Bitmap>?>(null) }
    var failed by remember { mutableStateOf(false) }
    // Incrementing this triggers re-rendering of the PDF pages
    var version by remember { mutableIntStateOf(0) }

    val pdfSavedMsg = stringResource(R.string.pdf_saved)
    val couldntSaveMsg = stringResource(R.string.couldnt_save_pdf)
    val savedToDownloadsMsg = stringResource(R.string.saved_to_downloads)

    fun restyle(newStyle: PdfStyle) {
        if (newStyle == style) return
        style = newStyle
        prefs.savePdfStyle(newStyle)
        scope.launch {
            val made = withContext(Dispatchers.IO) {
                val currentNote = note ?: return@withContext null
                NotePdf.write(context, site, currentNote, tables, adjustments, newStyle, viewModel.preparedBy.value)
            }
            if (made == null) {
                Toast.makeText(context, couldntSaveMsg, Toast.LENGTH_SHORT).show()
            } else {
                file = made
                version++
            }
        }
    }

    // Older Android versions save through the system "save as" picker instead of straight to Downloads
    val saveAs = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { runCatching { context.copyPdfTo(file, uri) }.isSuccess }
                Toast.makeText(context, if (ok) pdfSavedMsg else couldntSaveMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun download() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { runCatching { context.savePdfToDownloads(file) }.isSuccess }
                Toast.makeText(context, if (ok) savedToDownloadsMsg else couldntSaveMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            saveAs.launch(file.name)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val topSection = if (note?.isBill == false) stringResource(R.string.sites) else stringResource(R.string.bills)
        ScreenHeader(
            title = stringResource(R.string.preview),
            subtitle = file.nameWithoutExtension,
            onBack = onBack,
            trail = listOf(
                topSection,
                site?.site?.name.orEmpty(),
                note?.title.orEmpty()
            ),
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 8.dp, end = 20.dp)
        )
        CalmSegmented(
            options = PdfStyle.entries.map { stringResource(it.labelRes) },
            selectedIndex = style.ordinal,
            onSelect = { restyle(PdfStyle.entries[it]) },
            modifier = Modifier
                .widthIn(max = PAGE_MAX_WIDTH)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        )

        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val baseWidth = minOf(maxWidth - PAGE_GUTTER * 2, PAGE_MAX_WIDTH)
            val renderPx = minOf((baseWidth.value * density.density * 2f).toInt(), MAX_PAGE_PX)

            LaunchedEffect(version, renderPx) {
                pages = null
                failed = false
                val rendered = withContext(Dispatchers.IO) { runCatching { renderPages(file, renderPx) }.getOrNull() }
                if (rendered == null) failed = true else pages = rendered
            }

            val shown = pages
            when {
                failed -> Text(
                    stringResource(R.string.couldnt_open_pdf),
                    style = MaterialTheme.typography.bodyLarge,
                    color = InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
                shown == null -> CircularProgressIndicator(color = SageDeep, modifier = Modifier.padding(48.dp))
                else -> ZoomablePages(
                    pages = shown,
                    baseWidth = baseWidth.value,
                    viewportWidth = maxWidth.value,
                    viewportHeight = maxHeight.value
                )
            }
        }

        // Actions stay in reach at the bottom, whichever page is showing
        HorizontalDivider(color = SandLight)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Narrow phones and large text sizes: tighter padding and no wrapping keep both labels whole
            val actionPadding = PaddingValues(horizontal = 12.dp)
            OutlinedButton(
                onClick = ::download,
                enabled = pages != null,
                shape = CircleShape,
                border = BorderStroke(1.5.dp, if (pages != null) SageDeep else SandLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SageDeep),
                contentPadding = actionPadding,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) {
                Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.download), style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
            }
            Button(
                onClick = { context.sharePdf(file) },
                enabled = pages != null,
                shape = CircleShape,
                contentPadding = actionPadding,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
            ) {
                Icon(Icons.Outlined.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.share), style = MaterialTheme.typography.labelLarge, maxLines = 1, softWrap = false)
            }
        }
    }
}

/**
 * The pages, laid out at [baseWidth] times the zoom so ordinary scrolling works in both
 * directions when zoomed in. Two fingers pinch, a double-tap flips between 100% and 200%,
 * and the floating control steps through [ZOOM_STEPS]. All sizes are in dp.
 */
@Composable
private fun ZoomablePages(
    pages: List<Bitmap>,
    baseWidth: Float,
    viewportWidth: Float,
    viewportHeight: Float
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    var zoom by remember { mutableFloatStateOf(1f) }
    // Buttons and double-tap glide; pinching follows the fingers exactly
    var animateZoom by remember { mutableStateOf(false) }
    val shownZoom by animateFloatAsState(
        targetValue = zoom,
        animationSpec = if (animateZoom) tween(220) else tween(0),
        label = "pdfZoom"
    )
    val gutterPx = with(density) { PAGE_GUTTER.toPx() }

    /** Changes the zoom while keeping the point under [focus] (viewport px) in place. */
    fun zoomTo(target: Float, focus: Offset, animated: Boolean) {
        val old = zoom
        val new = target.coerceIn(MIN_ZOOM, MAX_ZOOM)
        if (new == old) return
        animateZoom = animated
        zoom = new
        val ratio = new / old
        // Content positions scale around the gutter, so anchor on the focus point inside the pages
        val targetX = ((hScroll.value + focus.x - gutterPx) * ratio + gutterPx - focus.x)
        val targetY = ((vScroll.value + focus.y) * ratio - focus.y)
        scope.launch {
            // Wait for the bigger layout before scrolling into it
            delay(if (animated) 230 else 16)
            hScroll.scrollTo(targetX.roundToInt().coerceAtLeast(0))
            vScroll.scrollTo(targetY.roundToInt().coerceAtLeast(0))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Two fingers zoom; one finger is left to the scroll containers underneath
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.count { it.pressed } >= 2) {
                            val change = event.calculateZoom()
                            if (change != 1f) {
                                zoomTo(zoom * change, event.calculateCentroid(useCurrent = true), animated = false)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { at -> zoomTo(if (zoom < 1.75f) 2f else 1f, at, animated = true) })
            }
    ) {
        val pageWidth = (baseWidth * shownZoom).dp
        val contentWidth = maxOf(viewportWidth.dp, pageWidth + PAGE_GUTTER * 2)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(vScroll)
                .horizontalScroll(hScroll)
                .width(contentWidth)
                .padding(top = 8.dp, bottom = 88.dp)
        ) {
            pages.forEachIndexed { index, page ->
                Image(
                    bitmap = page.asImageBitmap(),
                    contentDescription = stringResource(R.string.page_d_of_d, index + 1, pages.size),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .width(pageWidth)
                        .height(pageWidth * (page.height.toFloat() / page.width))
                        .shadow(6.dp, MaterialTheme.shapes.extraSmall)
                        .background(Color.White)
                        .border(1.dp, SandLight, MaterialTheme.shapes.extraSmall)
                )
                if (pages.size > 1) {
                    Text(
                        stringResource(R.string.page_d_of_d, index + 1, pages.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkMuted
                    )
                }
            }
        }

        // The zoom control floats over the pages, out of the way of the text
        val centre = with(density) { Offset(viewportWidth.dp.toPx() / 2, viewportHeight.dp.toPx() / 3) }
        val resetZoomLabel = stringResource(R.string.reset_zoom)
        val zoomPercentLabel = stringResource(R.string.zoom_d_percent, (zoom * 100).roundToInt())
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .shadow(10.dp, CircleShape)
                .background(Cover, CircleShape)
                .padding(4.dp)
        ) {
            IconButton(
                onClick = { zoomTo(ZOOM_STEPS.lastOrNull { it < zoom - 0.01f } ?: MIN_ZOOM, centre, animated = true) },
                enabled = zoom > MIN_ZOOM
            ) {
                Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.zoom_out), tint = if (zoom > MIN_ZOOM) Color.White else OnCoverMuted.copy(alpha = 0.4f))
            }
            Text(
                "${(zoom * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(min = 52.dp)
                    .clickable(role = Role.Button, onClickLabel = resetZoomLabel) { zoomTo(1f, centre, animated = true) }
                    .semantics { contentDescription = zoomPercentLabel }
                    .padding(vertical = 8.dp)
            )
            IconButton(
                onClick = { zoomTo(ZOOM_STEPS.firstOrNull { it > zoom + 0.01f } ?: MAX_ZOOM, centre, animated = true) },
                enabled = zoom < MAX_ZOOM
            ) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.zoom_in), tint = if (zoom < MAX_ZOOM) Color.White else OnCoverMuted.copy(alpha = 0.4f))
            }
        }
    }
}

/** Draws every page on white at [widthPx] wide. */
private fun renderPages(file: File, widthPx: Int): List<Bitmap> =
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { renderer ->
            (0 until renderer.pageCount).map { i ->
                renderer.openPage(i).use { page ->
                    val height = (widthPx.toFloat() * page.height / page.width).toInt()
                    Bitmap.createBitmap(widthPx, height, Bitmap.Config.ARGB_8888).also {
                        it.eraseColor(AndroidColor.WHITE)
                        page.render(it, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    }
                }
            }
        }
    }
