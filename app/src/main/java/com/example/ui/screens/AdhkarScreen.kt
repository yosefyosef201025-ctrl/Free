package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AdhanPlayer
import com.example.data.DhikrCategory
import com.example.data.DhikrDatabase
import com.example.data.DhikrItem
import com.example.ui.components.TasbihCounterView
import com.example.ui.theme.ActivePrayerGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight

@Composable
fun AdhkarScreen(
    totalTasbihCount: Int,
    onTasbihTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(DhikrCategory.TASBIH) }

    // Map to keep track of interactive progress on Adhkar cards
    val adhkarProgress = remember { mutableStateMapOf<String, Int>() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("adhkar_screen")
    ) {
        // Category Selector Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(DhikrCategory.entries) { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = {
                        Text(
                            text = cat.arabicTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Body
        if (selectedCategory == DhikrCategory.TASBIH) {
            // Interactive Digital Tasbih
            TasbihCounterView(
                totalLifetimeCount = totalTasbihCount,
                onTapHaptic = onTasbihTap,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 12.dp)
            )
        } else {
            // Adhkar List
            val items = when (selectedCategory) {
                DhikrCategory.MORNING -> DhikrDatabase.MORNING_ADHKAR
                DhikrCategory.EVENING -> DhikrDatabase.EVENING_ADHKAR
                DhikrCategory.POST_PRAYER -> DhikrDatabase.POST_PRAYER_ADHKAR
                else -> emptyList()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { dhikrItem ->
                    val count = adhkarProgress[dhikrItem.id] ?: 0
                    val isDone = count >= dhikrItem.targetCount

                    DhikrItemCard(
                        item = dhikrItem,
                        currentCount = count,
                        isDone = isDone,
                        onIncrement = {
                            if (!isDone) {
                                adhkarProgress[dhikrItem.id] = count + 1
                                onTasbihTap()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DhikrItemCard(
    item: DhikrItem,
    currentCount: Int,
    isDone: Boolean,
    onIncrement: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isDone) ActivePrayerGlow else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Arabic Text
            Text(
                text = item.textArabic,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Virtue / reference note
            if (item.virtue.isNotBlank()) {
                Text(
                    text = "• ${item.virtue}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = GoldAccent,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom Count Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التكرار: ${item.targetCount} مرات",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Button(
                    onClick = onIncrement,
                    enabled = !isDone,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDone) ActivePrayerGlow else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("dhikr_counter_button_${item.id}")
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("اكتمل", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    } else {
                        Text(
                            text = "$currentCount / ${item.targetCount}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
