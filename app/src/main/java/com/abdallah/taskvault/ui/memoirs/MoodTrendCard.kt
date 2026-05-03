package com.abdallah.taskvault.ui.memoirs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdallah.taskvault.R
import com.abdallah.taskvault.domain.model.Memoir
import java.util.Calendar

@Composable
fun MoodTrendCard(memoirs: List<Memoir>) {
    val monthMemoirs = remember(memoirs) {
        val calendar = Calendar.getInstance()
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        memoirs.filter { it.dateMillis >= startOfMonth }
    }

    if (monthMemoirs.isEmpty()) return

    val moodCounts = remember(monthMemoirs) {
        monthMemoirs.groupingBy { it.mood }.eachCount()
            .entries.sortedByDescending { it.value }.take(6)
    }
    val maxCount = moodCounts.maxOfOrNull { it.value } ?: 1

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.mood_trend_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${monthMemoirs.size} entries",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                moodCounts.forEach { (mood, count) ->
                    val fraction = count.toFloat() / maxCount
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(count.toString(), style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((60 * fraction).coerceAtLeast(4f).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f + 0.3f * fraction))
                        )
                        Text(mood, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
