package `in`.hridayan.driftly.export.pdf

object PdfDimensions {
    const val PAGE_WIDTH  = 1240
    const val PAGE_HEIGHT = 1754

    const val MARGIN_X        = 100f
    const val MARGIN_Y        = 100f
    const val INNER_MARGIN_X  = 140f

    // Vertical divider sits at 58% of content width
    val VERTICAL_DIVIDER_X = MARGIN_X + (PAGE_WIDTH - 2 * MARGIN_X) * 0.58f

    const val CONTENT_WIDTH = PAGE_WIDTH - (2 * MARGIN_X)
    const val CENTER_X      = PAGE_WIDTH / 2f

    // Heights
    const val HEADER_HEIGHT  = 230f   // title + subtitle + separator line + gap
    const val INFO_ROW_H     = 52f    // each info row height
    const val INFO_SECTION_H = 192f   // 2 rows + diamond divider + padding
    const val SUBJECT_BLOCK_H = 330f  // heading + 3 metric rows + separator + gap
    const val FOOTER_RESERVE  = 80f
}
