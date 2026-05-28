package `in`.hridayan.driftly.export.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import `in`.hridayan.driftly.export.pdf.model.AttendancePdfItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfSubjectRenderer {

    // ─────────────────────────────────────────
    //  HEADER
    // ─────────────────────────────────────────
    fun drawHeader(canvas: Canvas, manager: PdfPageManager) {
        val title    = "ATTENDANCE REPORT"
        val subtitle = "SELF ATTENDANCE"

        val titleWidth  = PdfPaints.titlePaint.measureText(title)
        val lineLen     = 150f
        val gapFromText = 32f

        val leftEnd    = PdfDimensions.CENTER_X - titleWidth / 2f - gapFromText
        val leftStart  = leftEnd - lineLen
        val rightStart = PdfDimensions.CENTER_X + titleWidth / 2f + gapFromText
        val rightEnd   = rightStart + lineLen
        val lineY      = manager.currentY - 16f

        canvas.drawLine(leftStart, lineY, leftEnd,   lineY, PdfPaints.solidLinePaint)
        canvas.drawLine(rightStart, lineY, rightEnd,  lineY, PdfPaints.solidLinePaint)
        PdfIconRenderer.drawDiamond(canvas, leftEnd  + gapFromText / 2f, lineY, 7f)
        PdfIconRenderer.drawDiamond(canvas, rightStart - gapFromText / 2f, lineY, 7f)

        canvas.drawText(title, PdfDimensions.CENTER_X, manager.currentY, PdfPaints.titlePaint)
        manager.advanceY(46f)

        canvas.drawText(subtitle, PdfDimensions.CENTER_X, manager.currentY, PdfPaints.subtitlePaint)
        manager.advanceY(52f)

        canvas.drawLine(
            PdfDimensions.MARGIN_X, manager.currentY,
            PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X, manager.currentY,
            PdfPaints.thinLinePaint
        )
        manager.advanceY(36f)
    }

    // ─────────────────────────────────────────
    //  STUDENT INFO (Name + Generated On)
    // ─────────────────────────────────────────
    fun drawStudentInfo(
        canvas: Canvas,
        manager: PdfPageManager,
        studentName: String,
        totalSubjects: Int  // kept in signature for compatibility
    ) {
        val iconCx      = PdfDimensions.INNER_MARGIN_X + 14f
        val labelX      = PdfDimensions.INNER_MARGIN_X + 46f
        val colonX      = labelX + 200f
        val valueX      = colonX + 24f
        val lineSpacing = PdfDimensions.INFO_ROW_H

        val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        PdfIconRenderer.drawPersonIcon(canvas, iconCx, manager.currentY - 12f)
        canvas.drawText("Student Name", labelX, manager.currentY, PdfPaints.infoLabelPaint)
        canvas.drawText(":", colonX, manager.currentY, PdfPaints.infoLabelPaint)
        canvas.drawText(studentName, valueX, manager.currentY, PdfPaints.infoValuePaint)
        manager.advanceY(lineSpacing)

        PdfIconRenderer.drawCalendarIcon(canvas, iconCx, manager.currentY - 12f)
        canvas.drawText("Generated On", labelX, manager.currentY, PdfPaints.infoLabelPaint)
        canvas.drawText(":", colonX, manager.currentY, PdfPaints.infoLabelPaint)
        canvas.drawText(dateString, valueX, manager.currentY, PdfPaints.infoValuePaint)
        manager.advanceY(lineSpacing)

        manager.advanceY(20f)

        val sepY = manager.currentY
        canvas.drawLine(PdfDimensions.MARGIN_X, sepY, PdfDimensions.CENTER_X - 24f, sepY, PdfPaints.solidLinePaint)
        PdfIconRenderer.drawDiamond(canvas, PdfDimensions.CENTER_X, sepY, 8f)
        canvas.drawLine(PdfDimensions.CENTER_X + 24f, sepY, PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X, sepY, PdfPaints.solidLinePaint)
        manager.advanceY(48f)
    }

    // ─────────────────────────────────────────
    //  HELPER: draw wrapped subject heading
    //  Returns the total Y advance consumed.
    // ─────────────────────────────────────────
    private fun drawHeading(
        canvas: Canvas,
        manager: PdfPageManager,
        text: String,
        startX: Float,
        maxWidth: Float
    ): Float {
        val paint     = PdfPaints.subjectTitlePaint
        val lineH     = 44f   // advance per line (matches single-line advance)

        // Fast path: fits on one line
        if (paint.measureText(text) <= maxWidth) {
            canvas.drawText(text, startX, manager.currentY, paint)
            manager.advanceY(lineH)
            return lineH
        }

        // Slow path: wrap with StaticLayout
        val tp     = TextPaint(paint)
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, tp, maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(6f, 1f)   // 6px extra between wrapped lines
            .setIncludePad(false)
            .build()

        // StaticLayout draws from (0,0) = top-left of the text box.
        // We want the first line's baseline to sit at manager.currentY,
        // so we translate y by paint.ascent() (a negative value = move up).
        canvas.save()
        canvas.translate(startX, manager.currentY + paint.ascent())
        layout.draw(canvas)
        canvas.restore()

        // Advance: each extra line adds lineH; the first line's advance is already lineH
        val totalAdvance = lineH + (layout.lineCount - 1) * lineH
        manager.advanceY(totalAdvance)
        return totalAdvance
    }

    // ─────────────────────────────────────────
    //  SUBJECT BLOCK
    // ─────────────────────────────────────────
    fun drawSubjectBlock(
        canvas: Canvas,
        manager: PdfPageManager,
        index: Int,
        item: AttendancePdfItem
    ) {
        val startX   = PdfDimensions.MARGIN_X
        val dividerX = PdfDimensions.VERTICAL_DIVIDER_X
        val rightCX  = dividerX + (PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X - dividerX) / 2f

        // ── Subject heading (wraps if too long) ──
        val heading  = "$index.  ${item.subjectName}"
        val maxWidth = PdfDimensions.PAGE_WIDTH - startX - PdfDimensions.MARGIN_X
        drawHeading(canvas, manager, heading, startX, maxWidth)

        val metricsTopY = manager.currentY
        val iconCx      = startX + 18f
        val labelX      = startX + 50f
        val valueX      = dividerX - 36f
        val rowH        = 56f

        // ── Helper: metric row ───────────────
        fun metricRow(
            iconFn: (Canvas, Float, Float) -> Unit,
            label: String,
            value: String
        ) {
            val baseLine = manager.currentY
            iconFn(canvas, iconCx, baseLine - 10f)
            canvas.drawText(label, labelX, baseLine, PdfPaints.subjectLabelPaint)

            val labelW     = PdfPaints.subjectLabelPaint.measureText(label)
            val valueW     = PdfPaints.subjectValuePaint.measureText(value)
            val dashStartX = labelX + labelW + 10f
            val dashEndX   = valueX - valueW - 10f
            if (dashEndX > dashStartX + 20f) {
                canvas.drawLine(dashStartX, baseLine - 7f, dashEndX, baseLine - 7f, PdfPaints.dashLinePaint)
            }
            canvas.drawText(value, valueX, baseLine, PdfPaints.subjectValuePaint)
            manager.advanceY(rowH)
        }

        metricRow(PdfIconRenderer::drawCheckIcon,  "Attended Classes", item.attendedClasses.toString())
        metricRow(PdfIconRenderer::drawXIcon,       "Missed Classes",   item.missedClasses.toString())
        metricRow(PdfIconRenderer::drawTargetIcon,  "Target Threshold", "${item.threshold}%")

        val metricsBottomY = manager.currentY - rowH * 0.12f

        // ── Vertical divider line ────────────
        canvas.drawLine(
            dividerX, metricsTopY - 18f,
            dividerX, metricsBottomY + 8f,
            PdfPaints.thinLinePaint
        )

        // ── Right side: percentage centred in metrics area ──
        val blockMidY   = (metricsTopY + metricsBottomY) / 2f
        val numBaseline = blockMidY + 35f

        // Format to decimal if not a whole number, otherwise keep as integer
        val formattedPercent = if (item.percentage % 1f == 0f) {
            String.format(Locale.getDefault(), "%.0f", item.percentage)
        } else {
            String.format(Locale.getDefault(), "%.2f", item.percentage)
        }

        val intPart = formattedPercent.substringBefore('.')
        val decPart = if (formattedPercent.contains('.')) "." + formattedPercent.substringAfter('.') else ""

        val originalAlign = PdfPaints.percentagePaint.textAlign
        PdfPaints.percentagePaint.textAlign = Paint.Align.LEFT

        val intW = PdfPaints.percentagePaint.measureText(intPart)
        val decW = if (decPart.isNotEmpty()) PdfPaints.percentageDecimalPaint.measureText(decPart) else 0f
        val symW = PdfPaints.percentageSymbolPaint.measureText("%")
        val gap = 6f
        val totalW = intW + decW + symW + gap

        val percentStartX = rightCX - totalW / 2f

        // 1. Draw Integer Part
        canvas.drawText(intPart, percentStartX, numBaseline, PdfPaints.percentagePaint)

        // 2. Draw Decimal Part (if any)
        if (decPart.isNotEmpty()) {
            canvas.drawText(decPart, percentStartX + intW, numBaseline, PdfPaints.percentageDecimalPaint)
        }

        // 3. Draw Percentage Symbol (raised)
        canvas.drawText("%", percentStartX + intW + decW + gap, numBaseline - 54f, PdfPaints.percentageSymbolPaint)

        // Restore original alignment
        PdfPaints.percentagePaint.textAlign = originalAlign

        manager.advanceY(28f)

        // ── Dashed horizontal separator ──────
        canvas.drawLine(
            PdfDimensions.MARGIN_X, manager.currentY,
            PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X, manager.currentY,
            PdfPaints.separatorDashPaint
        )
        manager.advanceY(56f)
    }

    // ─────────────────────────────────────────
    //  FOOTER
    // ─────────────────────────────────────────
    fun drawFooter(canvas: Canvas, manager: PdfPageManager, totalPages: Int) {
        val footerY = PdfDimensions.PAGE_HEIGHT - PdfDimensions.MARGIN_Y
        canvas.drawLine(
            PdfDimensions.MARGIN_X, footerY - 28f,
            PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X, footerY - 28f,
            PdfPaints.solidLinePaint
        )
        canvas.drawText("Generated by Self Attendance", PdfDimensions.MARGIN_X, footerY, PdfPaints.footerPaint)
        canvas.drawText(
            "Page ${manager.getCurrentPageNumber()} of $totalPages",
            PdfDimensions.PAGE_WIDTH - PdfDimensions.MARGIN_X, footerY, PdfPaints.footerRightPaint
        )
    }
}
