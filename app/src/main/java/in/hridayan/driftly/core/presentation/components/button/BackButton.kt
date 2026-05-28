@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.driftly.core.presentation.components.button

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.driftly.R
import `in`.hridayan.driftly.core.common.LocalWeakHaptic
import `in`.hridayan.driftly.core.presentation.components.tooltip.TooltipContent
import `in`.hridayan.driftly.navigation.LocalNavController

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current

    TooltipContent(stringResource(R.string.back_button)) {
        FilledTonalIconButton(
            onClick = {
                weakHaptic()
                if (onClick != null) {
                    onClick()
                } else {
                    navController.popBackStack()
                }
            },
            shapes = IconButtonDefaults.shapes(),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                modifier = modifier.size(24.dp),
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null
            )
        }
    }
}