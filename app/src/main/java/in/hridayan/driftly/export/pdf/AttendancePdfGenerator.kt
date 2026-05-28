package `in`.hridayan.driftly.export.pdf

import android.content.Context
import android.graphics.pdf.PdfDocument
import `in`.hridayan.driftly.export.pdf.model.AttendancePdfItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AttendancePdfGenerator {

    suspend fun generatePdf(
        context: Context,
        studentName: String,
        subjects: List<AttendancePdfItem>
    ): File? = withContext(Dispatchers.IO) {
        if (subjects.isEmpty()) return@withContext null

        // ── Dry-run to count total pages ──────────────────────────────────────
        val totalPages = dryRunPageCount(studentName, subjects)

        // ── Real render ───────────────────────────────────────────────────────
        val document   = PdfDocument()
        val pageManager = PdfPageManager(document)

        try {
            pageManager.startNewPage()

            // Header + info section
            pageManager.canvas?.let { c ->
                PdfSubjectRenderer.drawHeader(c, pageManager)
            }

            // Subject blocks
            subjects.forEachIndexed { idx, item ->
                val blockH = PdfDimensions.SUBJECT_BLOCK_H

                if (pageManager.newPageNeeded(blockH)) {
                    // Footer on the page we are leaving
                    pageManager.canvas?.let { c ->
                        PdfSubjectRenderer.drawFooter(c, pageManager, totalPages)
                    }
                    pageManager.startNewPage()
                    pageManager.advanceY(40f)
                }

                pageManager.canvas?.let { c ->
                    PdfSubjectRenderer.drawSubjectBlock(c, pageManager, idx + 1, item)
                }
            }

            // Footer on final page
            pageManager.canvas?.let { c ->
                PdfSubjectRenderer.drawFooter(c, pageManager, totalPages)
            }

            pageManager.finishCurrentPage()

            // Write to file
            val file = PdfUtils.createPdfFile(context)
            FileOutputStream(file).use { out -> document.writeTo(out) }
            return@withContext file

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            document.close()
        }
    }

    /** Simulate layout without actually drawing to find total page count. */
    private fun dryRunPageCount(studentName: String, subjects: List<AttendancePdfItem>): Int {
        var pageNum = 1
        // header section only (no info section)
        var y = PdfDimensions.MARGIN_Y +
                PdfDimensions.HEADER_HEIGHT

        val usablePage = PdfDimensions.PAGE_HEIGHT -
                PdfDimensions.MARGIN_Y -
                PdfDimensions.FOOTER_RESERVE

        val headingMaxWidth = PdfDimensions.CONTENT_WIDTH

        subjects.forEachIndexed { idx, item ->
            // Estimate heading lines: 1 line = 44f, each extra line adds 44f
            val headingText  = "${idx + 1}.  ${item.subjectName}"
            val headingWidth = PdfPaints.subjectTitlePaint.measureText(headingText)
            val extraLines   = if (headingWidth > headingMaxWidth)
                                   Math.ceil((headingWidth / headingMaxWidth).toDouble()).toInt() - 1
                               else 0
            val blockH = PdfDimensions.SUBJECT_BLOCK_H + extraLines * 44f

            if (y + blockH > usablePage) {
                pageNum++
                y = PdfDimensions.MARGIN_Y + 40f
            }
            y += blockH
        }
        return pageNum
    }
}
