package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.PrayerTimeItem
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight

@Composable
fun PrayerCountdownCard(
    dateHijri: String,
    dateGregorian: String,
    cityName: String,
    nextPrayer: PrayerTimeItem?,
    remainingMillis: Long,
    progress: Float,
    isAudioPlaying: Boolean,
    onCityClick: () -> Unit,
    onToggleAdhanPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f), label = "prayer_progress")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, GoldAccent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .testTag("prayer_countdown_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp)
        ) {
            // Background Image
            Image(
                painter = painterResource(id = R.drawable.img_mosque_header),
                contentDescription = "Mosque Header Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Spiritual gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC081511),
                                Color(0xEE0B1E18),
                                Color(0xFF07120F)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar: City & Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // City selector pill
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .clickable { onCityClick() }
                            .testTag("city_selector_pill")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = GoldAccentLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = cityName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    // Hijri Date
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = dateHijri,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = GoldAccentLight,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = dateGregorian,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.White.copy(alpha = 0.75f)
                            )
                        )
                    }
                }

                // Middle: Next Prayer Countdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "الصلاة القادمة",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = GoldAccentLight.copy(alpha = 0.9f)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = nextPrayer?.type?.arabicName ?: "الفجر",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 34.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "في تمام ${nextPrayer?.timeFormatted ?: "--:--"}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    // Countdown Timer Dial
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(112.dp)
                            .testTag("countdown_dial")
                    ) {
                        CircularProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                            color = GoldAccent,
                            trackColor = Color(0x33FFFFFF)
                        )

                        val hours = (remainingMillis / (1000 * 60 * 60)).coerceAtLeast(0)
                        val minutes = ((remainingMillis / (1000 * 60)) % 60).coerceAtLeast(0)
                        val seconds = ((remainingMillis / 1000) % 60).coerceAtLeast(0)

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "متبقي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            )
                            Text(
                                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }

                // Bottom: Quick audio test button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "﴿ حَافِظُوا عَلَى الصَّلَوَاتِ وَالصَّلَاةِ الْوُسْطَىٰ ﴾",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = GoldAccentLight.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium
                        )
                    )

                    // Audio play preview button
                    FilledTonalButton(
                        onClick = onToggleAdhanPreview,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isAudioPlaying) Color(0xFFDC2626) else GoldAccent,
                            contentColor = if (isAudioPlaying) Color.White else Color(0xFF1E1400)
                        ),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("adhan_preview_button")
                    ) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isAudioPlaying) "Stop Adhan" else "Play Adhan",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isAudioPlaying) "إيقاف" else "سماع الأذان",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
