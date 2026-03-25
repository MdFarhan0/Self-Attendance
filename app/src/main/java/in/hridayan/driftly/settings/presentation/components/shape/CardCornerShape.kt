package `in`.hridayan.driftly.settings.presentation.components.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

object CardCornerShape {
    val SINGLE_CARD = RoundedCornerShape(20.dp)

    val FIRST_CARD = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = 5.dp,
        bottomEnd = 5.dp
    )

    val MIDDLE_CARD = RoundedCornerShape(5.dp)

    val LAST_CARD = RoundedCornerShape(
        topStart = 5.dp,
        topEnd = 5.dp,
        bottomStart = 20.dp,
        bottomEnd = 20.dp
    )

    fun getRoundedShape(
        index: Int,
        size: Int,
    ): RoundedCornerShape {
        return when {
            size == 1 -> SINGLE_CARD

            index == 0 -> FIRST_CARD

            index == size - 1 -> LAST_CARD

            else -> MIDDLE_CARD
        }
    }
}
