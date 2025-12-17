package `in`.hridayan.driftly.home.presentation.components.histogram

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.core.data.model.SubjectEntity
import kotlinx.coroutines.delay

data class SubjectHistogramData(
    val subject: SubjectEntity,
    val percentage: Float
)

@Composable
fun AttendanceHistogramCard(
    modifier: Modifier = Modifier,
    histogramData: List<SubjectHistogramData>
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp),
        shape = RoundedCornerShape(25.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = "Subject Performance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (histogramData.isEmpty()) {
                Text(
                    text = "No subjects yet. Add a subject to see performance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    histogramData.forEachIndexed { index, data ->
                        HistogramBar(
                            label = data.subject.histogramLabel
                                ?: data.subject.subject.take(5),
                            percentage = data.percentage,
                            animationDelay = index * 100L // Stagger animation
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistogramBar(
    label: String,
    percentage: Float,
    animationDelay: Long = 0L
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger entrance animation with delay
    LaunchedEffect(Unit) {
        delay(animationDelay)
        isVisible = true
    }
    
    val animatedHeight by animateFloatAsState(
        targetValue = if (isVisible) percentage else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bar_height_animation"
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(500)) + 
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Percentage text at top
            Text(
                text = "${String.format("%.0f", animatedHeight)}%",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            // Histogram bar
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(150.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                val barHeight = (animatedHeight.coerceIn(0f, 100f) * 1.5f).dp

                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                        .background(
                            MaterialTheme.colorScheme.primary
                        )
                )
            }

            // Subject label at bottom
            Text(
                text = label.uppercase().take(5),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(60.dp)
            )
        }
    }
}
