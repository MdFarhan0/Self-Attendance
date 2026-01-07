package `in`.hridayan.driftly.core.presentation.components.picker

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    items: List<String>,
    initialIndex: Int,
    visibleItemsCount: Int = 3,
    itemHeight: Dp = 40.dp,
    modifier: Modifier = Modifier,
    onSelectionChanged: (Int) -> Unit
) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }
    
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = (listState.firstVisibleItemIndex) + visibleItemsCount / 2
            // Calculate correct index based on offset
             // This is a simplified approach; true snapping requires more precise math
             // But SnapFlingBehavior handles the physical snap. We just need to report the index.
             // With visibleItemsCount 3, the selected item is index + 1
             val selectedIndex = listState.firstVisibleItemIndex
             if (selectedIndex in items.indices) {
                 onSelectionChanged(selectedIndex)
             }
        }
    }
    
    // Notify selection change immediately when scrolling stops or settles
    val currentFirstIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    LaunchedEffect(currentFirstIndex) {
        onSelectionChanged(currentFirstIndex)
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemsCount),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items.size) { index ->
                val opacity by remember {
                    derivedStateOf {
                        val currentItemInfo = listState.layoutInfo.visibleItemsInfo
                            .find { it.index == index }
                            ?: return@derivedStateOf 0.5f
                        val itemOffset = currentItemInfo.offset - listState.layoutInfo.viewportStartOffset
                        val centerOffset = listState.layoutInfo.viewportSize.height / 2
                        val distance = kotlin.math.abs(itemOffset + currentItemInfo.size / 2 - centerOffset)
                        val maxDistance = itemHeightPx * (visibleItemsCount / 2)
                        
                        1f - (distance / maxDistance).coerceIn(0f, 0.5f)
                    }
                }
                
                val scale by remember {
                    derivedStateOf {
                         val currentItemInfo = listState.layoutInfo.visibleItemsInfo
                            .find { it.index == index }
                            ?: return@derivedStateOf 0.8f
                        val itemOffset = currentItemInfo.offset - listState.layoutInfo.viewportStartOffset
                        val centerOffset = listState.layoutInfo.viewportSize.height / 2
                        val distance = kotlin.math.abs(itemOffset + currentItemInfo.size / 2 - centerOffset)
                        
                        if (distance < itemHeightPx / 2) 1.1f else 0.8f
                    }
                }

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .width(60.dp) // Fixed width for alignment
                        .scale(scale)
                        .alpha(opacity),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[index],
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (scale > 1f) FontWeight.Bold else FontWeight.Normal,
                        color = if (scale > 1f) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Optional: Selection Indicator overlay
        // Box(modifier = Modifier.fillMaxWidth().height(itemHeight).background(Color.Gray.copy(alpha = 0.1f)))
    }
}
