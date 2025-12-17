package `in`.hridayan.driftly.core.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object Shape {
    val labelStroke = 1.dp

    val cardStrokeSmall = 1.dp
    val cardStrokeMedium = 2.dp
    val cardStrokeLarge = 4.dp

    val cardCornerSmall = RoundedCornerShape(8.dp)
    val cardCornerMedium = RoundedCornerShape(25.dp)
    val cardCornerLarge = RoundedCornerShape(25.dp)

    val cardTopCornersRounded = RoundedCornerShape(
        topStart = 25.dp,
        topEnd = 25.dp,
        bottomStart = 2.dp,
        bottomEnd = 2.dp
    )

    val cardBottomCornersRounded = RoundedCornerShape(
        topStart = 2.dp,
        topEnd = 2.dp,
        bottomStart = 25.dp,
        bottomEnd = 25.dp
    )
}