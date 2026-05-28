package `in`.hridayan.driftly.export.pdf

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF

/**
 * Draws small vector icons using Canvas primitives — zero emoji, guaranteed to render in PDFs.
 * Every icon is ~26 px tall, centred on (cx, cy).
 */
object PdfIconRenderer {

    private const val S = 13f   // half-size of the icon bounding box

    // ── Person silhouette — Student Name ──────────────────────────────────────
    fun drawPersonIcon(canvas: Canvas, cx: Float, cy: Float) {
        val r = S * 0.38f
        // Head
        canvas.drawCircle(cx, cy - S * 0.30f, r, PdfPaints.iconFillPaint)
        // Shoulder arc
        val bodyRect = RectF(cx - S * 0.60f, cy - S * 0.30f + r,
                             cx + S * 0.60f, cy + S * 0.90f)
        val path = Path().apply {
            moveTo(cx - S * 0.60f, cy + S * 0.90f)
            arcTo(bodyRect, 180f, -180f)
            close()
        }
        canvas.drawPath(path, PdfPaints.iconFillPaint)
    }

    // ── Calendar — Generated On ───────────────────────────────────────────────
    fun drawCalendarIcon(canvas: Canvas, cx: Float, cy: Float) {
        val left   = cx - S * 0.75f
        val right  = cx + S * 0.75f
        val top    = cy - S * 0.65f
        val bottom = cy + S * 0.75f
        val hdrBot = top + S * 0.48f

        // Outer box
        canvas.drawRect(left, top, right, bottom, PdfPaints.iconPaint)
        // Filled header bar
        val hdr = Path().apply {
            moveTo(left, top); lineTo(right, top)
            lineTo(right, hdrBot); lineTo(left, hdrBot); close()
        }
        canvas.drawPath(hdr, PdfPaints.iconFillPaint)

        // Inner grid lines
        val midX = (left + right)  / 2f
        val midY = (hdrBot + bottom) / 2f
        canvas.drawLine(midX,  hdrBot, midX,  bottom, PdfPaints.thinLinePaint)
        canvas.drawLine(left,  midY,   right, midY,   PdfPaints.thinLinePaint)

        // Hanger tabs
        val tabPaint = Paint(PdfPaints.solidLinePaint).apply { strokeWidth = 3f }
        canvas.drawLine(cx - S * 0.35f, top - S * 0.28f, cx - S * 0.35f, top + S * 0.14f, tabPaint)
        canvas.drawLine(cx + S * 0.35f, top - S * 0.28f, cx + S * 0.35f, top + S * 0.14f, tabPaint)
    }

    // ── Check mark in circle — Attended ──────────────────────────────────────
    fun drawCheckIcon(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawCircle(cx, cy, S * 0.75f, PdfPaints.iconPaint)
        val tickPaint = Paint(PdfPaints.iconPaint).apply { strokeWidth = 3f }
        val path = Path().apply {
            moveTo(cx - S * 0.35f, cy)
            lineTo(cx - S * 0.05f, cy + S * 0.35f)
            lineTo(cx + S * 0.45f, cy - S * 0.30f)
        }
        canvas.drawPath(path, tickPaint)
    }

    // ── X mark in circle — Missed ─────────────────────────────────────────────
    fun drawXIcon(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawCircle(cx, cy, S * 0.75f, PdfPaints.iconPaint)
        val xPaint = Paint(PdfPaints.iconPaint).apply { strokeWidth = 3f }
        val d = S * 0.32f
        canvas.drawLine(cx - d, cy - d, cx + d, cy + d, xPaint)
        canvas.drawLine(cx + d, cy - d, cx - d, cy + d, xPaint)
    }

    // ── Target / bullseye — Threshold ─────────────────────────────────────────
    fun drawTargetIcon(canvas: Canvas, cx: Float, cy: Float) {
        val p = Paint(PdfPaints.iconPaint).apply { strokeWidth = 2f }
        canvas.drawCircle(cx, cy, S * 0.75f, p)
        canvas.drawCircle(cx, cy, S * 0.44f, p)
        canvas.drawCircle(cx, cy, S * 0.15f, PdfPaints.iconFillPaint)
    }

    // ── Small filled diamond ❖ ────────────────────────────────────────────────
    fun drawDiamond(canvas: Canvas, cx: Float, cy: Float, size: Float = 10f) {
        val path = Path().apply {
            moveTo(cx,              cy - size)
            lineTo(cx + size * 0.6f, cy)
            lineTo(cx,              cy + size)
            lineTo(cx - size * 0.6f, cy)
            close()
        }
        canvas.drawPath(path, PdfPaints.iconFillPaint)
    }
}
