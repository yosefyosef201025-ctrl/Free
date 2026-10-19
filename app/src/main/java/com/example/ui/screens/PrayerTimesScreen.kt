package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.NotificationsActive
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
import com.example.model.PrayerTimeItem
import com.example.model.PrayerType
import com.example.ui.AppScreen
import com.example.ui.MainUiState
import com.example.ui.components.PrayerCountdownCard
import com.example.ui.components.PrayerTimeRow
import com.example.ui.theme.ActivePrayerGlow
import com.example.ui.theme.GoldAccent

@Composable
fun PrayerTimesScreen(
    uiState: MainUiState,
    onCityClick: () -> Unit,
    onToggleAdhanPreview: () -> Unit,
    onAlertClick: (PrayerType) -> Unit,
    onToggleCheckedDone: (PrayerType) -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayer_times_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner with Countdown
        item {
            PrayerCountdownCard(
                dateHijri = uiState.dateHijri,
                dateGregorian = uiState.dateGregorian,
                cityName = if (uiState.isUsingGps) "موقعي الحالي (GPS)" else uiState.selectedCity.arabicName,
                nextPrayer = uiState.nextPrayer,
                remainingMillis = uiState.remainingMillis,
                progress = uiState.countdownProgress,
                isAudioPlaying = uiState.isAudioPlaying,
                onCityClick = onCityClick,
                onToggleAdhanPreview = onToggleAdhanPreview
            )
        }

        // Daily Prayer Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_prayer_tracker_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "متابعة صلوات اليوم المكتوبة",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = uiState.completedPrayersCount / 5f
                        LinearProgressIndicator(
                            progress = { progress },
                            color = ActivePrayerGlow,
                            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50))
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${uiState.completedPrayersCount} من 5",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Section Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مواقيت الصلوات الخمس والشروق",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Text(
                    text = "المذهب: ${uiState.juristicMethod.arabicName.split(" ")[0]}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        // Prayer Time Rows
        items(uiState.prayerTimes, key = { it.type.name }) { prayerItem ->
            PrayerTimeRow(
                item = prayerItem,
                onAlertClick = { onAlertClick(prayerItem.type) },
                onToggleCheckedDone = { onToggleCheckedDone(prayerItem.type) }
            )
        }

        // Quick feature navigation pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilledTonalButton(
                    onClick = { onNavigate(AppScreen.QIBLA) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("nav_to_qibla_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.CompassCalibration,
                        contentDescription = "Qibla",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("اتجاه القبلة", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                FilledTonalButton(
                    onClick = { onNavigate(AppScreen.SETTINGS) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("nav_to_settings_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ضبط التنبيهات", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }
    }
}
