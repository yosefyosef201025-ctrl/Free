package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AlertMode
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.theme.ActivePrayerGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight

@Composable
fun PrayerTimeRow(
    item: PrayerTimeItem,
    onAlertClick: () -> Unit,
    onToggleCheckedDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNext = item.isNext
    val isChecked = item.isCheckedDone

    val containerColor = when {
        isNext -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        isChecked -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isNext -> GoldAccent
        isChecked -> Color(0x3310B981)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(if (isNext) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(18.dp))
            .testTag("prayer_row_${item.type.name}"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 4.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left (in LTR) / Right (in RTL): Icon & Prayer Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Done checkmark button
                IconButton(
                    onClick = onToggleCheckedDone,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("check_done_${item.type.name}")
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Toggle Prayer Completed",
                        tint = if (isChecked) ActivePrayerGlow else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Prayer Name & Badge
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.type.arabicName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (isNext) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "القادمة",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Color(0xFF221600),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (item.type == PrayerType.SUNRISE) {
                        Text(
                            text = "وقت شروق الشمس وانتهاء الفجر",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Right side: Time and Alert Config Button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.timeFormatted,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (isNext) GoldAccent else MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Alert Mode Indicator & Click Target
                Surface(
                    color = if (item.alertConfig.isEnabled) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    },
                    shape = CircleShape,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .clickable { onAlertClick() }
                        .testTag("alert_toggle_${item.type.name}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when {
                            !item.alertConfig.isEnabled -> Icons.Default.NotificationsOff
                            item.alertConfig.alertMode == AlertMode.ADHAN -> Icons.Default.VolumeUp
                            item.alertConfig.alertMode == AlertMode.TAKBEER -> Icons.Default.Campaign
                            item.alertConfig.alertMode == AlertMode.CHIME -> Icons.Default.NotificationsActive
                            item.alertConfig.alertMode == AlertMode.VIBRATE -> Icons.Default.Vibration
                            else -> Icons.Default.NotificationsNone
                        }

                        val tint = if (item.alertConfig.isEnabled) {
                            if (isNext) GoldAccent else MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = "Alert Settings for ${item.type.arabicName}",
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
