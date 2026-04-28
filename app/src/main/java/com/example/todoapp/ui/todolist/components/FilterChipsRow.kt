package com.example.todoapp.ui.todolist.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.todoapp.R
import com.example.todoapp.ui.todolist.FilterOption

@Composable
fun FilterChipsRow(
    selectedFilter: FilterOption,
    onFilterSelected: (FilterOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        FilterOption.entries.forEach { filter ->
            val selected = selectedFilter == filter
            val containerColor by animateColorAsState(
                targetValue = if (selected)
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200),
                label = "chipColor"
            )
            val labelColor by animateColorAsState(
                targetValue = if (selected)
                    MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "chipLabelColor"
            )
            FilterChip(
                selected = selected,
                onClick  = { onFilterSelected(filter) },
                label    = {
                    Text(
                        text  = stringResource(filter.displayNameRes()),
                        color = labelColor
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor    = containerColor,
                    selectedLabelColor        = labelColor
                )
            )
        }
    }
}

private fun FilterOption.displayNameRes(): Int = when (this) {
    FilterOption.ALL       -> R.string.filter_all
    FilterOption.ACTIVE    -> R.string.filter_active
    FilterOption.COMPLETED -> R.string.filter_completed
}
