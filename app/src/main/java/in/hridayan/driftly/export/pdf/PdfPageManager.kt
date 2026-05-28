package `in`.hridayan.driftly.export.pdf

import android.graphics.Canvas
import android.graphics.pdf.PdfDocument

class PdfPageManager(private val document: PdfDocument) {

    private var currentPageNumber = 1
    private var currentPage: PdfDocument.Page? = null

    var canvas: Canvas? = null
        private set

    var currentY = 0f
        private set

    fun startNewPage() {
        finishCurrentPage()

        val pageInfo = PdfDocument.PageInfo.Builder(
            PdfDimensions.PAGE_WIDTH,
            PdfDimensions.PAGE_HEIGHT,
            currentPageNumber
        ).create()

        currentPage = document.startPage(pageInfo)
        canvas = currentPage?.canvas

        // White background
        currentPage?.canvas?.drawColor(android.graphics.Color.WHITE)

        currentY = PdfDimensions.MARGIN_Y
    }

    fun finishCurrentPage() {
        currentPage?.let { document.finishPage(it) }
        currentPage = null
        canvas = null
    }

    fun advanceY(amount: Float) {
        currentY += amount
    }

    /**
     * Returns true if adding [requiredHeight] would overflow the page (minus footer reserve).
     * Does NOT increment page number; the caller must call startNewPage().
     */
    fun wouldOverflow(requiredHeight: Float): Boolean {
        return currentY + requiredHeight >
                PdfDimensions.PAGE_HEIGHT - PdfDimensions.MARGIN_Y - PdfDimensions.FOOTER_RESERVE
    }

    fun newPageNeeded(requiredHeight: Float): Boolean {
        if (wouldOverflow(requiredHeight)) {
            currentPageNumber++
            return true
        }
        return false
    }

    fun getCurrentPageNumber(): Int = currentPageNumber
}
