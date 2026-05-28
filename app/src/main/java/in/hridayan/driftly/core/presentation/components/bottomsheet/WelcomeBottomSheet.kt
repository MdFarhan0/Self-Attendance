@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.driftly.core.presentation.components.bottomsheet

import android.annotation.SuppressLint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.driftly.BuildConfig
import `in`.hridayan.driftly.core.presentation.theme.adaptiveModalScrimColor
import kotlin.math.sin

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun WelcomeBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        scrimColor = adaptiveModalScrimColor(),
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.padding(horizontal = 15.dp, vertical = 16.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp)
                .padding(bottom = 28.dp, top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Version Chip
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            // Material 3 Expressive Wave Motion
            WelcomeWaveMotion(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = screenHeight * 0.6f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                val features = listOf(
                    Triple(
                        Icons.Rounded.PictureAsPdf,
                        "PDF Export",
                        "Generate precise and professional offline PDF attendance reports with a clean and polished layout."
                    ),
                    Triple(
                        Icons.Rounded.AutoAwesome,
                        "Stunning UI",
                        "Introduced multiple under-the-hood UI improvements and refined the overall experience closer to Material 3 Expressive design."
                    ),
                    Triple(
                        Icons.Rounded.DeleteOutline,
                        "Removed Bloat",
                        "Cleaned up unused and unnecessary code, reducing the APK size from 6 MB to 3.8 MB for a lighter and more optimized app experience."
                    ),
                    Triple(
                        Icons.Rounded.Animation,
                        "Micro Animations",
                        "Added smooth micro animations across the app, including buttons and interactions, for a more fluid and responsive feel."
                    ),
                    Triple(
                        Icons.Rounded.Tune,
                        "More Customization & Features",
                        "Explore Settings to discover under-the-hood customizations, improvements, and newly added features."
                    )
                )

                items(features.size) { index ->
                    val feature = features[index]
                    val isFirst = index == 0
                    val isLast = index == features.lastIndex

                    val shape = when {
                        isFirst -> RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp, bottomStart = 3.dp, bottomEnd = 3.dp)
                        isLast -> RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp, bottomStart = 15.dp, bottomEnd = 15.dp)
                        else -> RoundedCornerShape(3.dp)
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = shape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = feature.first,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = feature.second,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = feature.third,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeWaveMotion(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val points = 50
        
        val path1 = Path()
        val path2 = Path()
        
        for (i in 0..points) {
            val x = (i.toFloat() / points) * width
            
            // First wave
            val y1 = height / 2 + sin(i.toFloat() / 4.5f + waveOffset) * (height / 3)
            if (i == 0) path1.moveTo(x, y1) else path1.lineTo(x, y1)
            
            // Second wave (offset phase and inverted)
            val y2 = height / 2 + sin(i.toFloat() / 4.5f - waveOffset + Math.PI.toFloat()) * (height / 3)
            if (i == 0) path2.moveTo(x, y2) else path2.lineTo(x, y2)
        }

        // Draw secondary (faint) wave
        drawPath(
            path = path2,
            color = secondaryColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Draw primary (solid) wave
        drawPath(
            path = path1,
            color = primaryColor,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
