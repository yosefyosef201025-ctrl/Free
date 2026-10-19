package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculation.QiblaCalculator
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.QiblaGreenGlow
import kotlin.math.*

@Composable
fun QiblaCompassView(
    compassHeading: Float,
    qiblaBearing: Double,
    distanceKaabaKm: Double,
    isSensorAvailable: Boolean,
    onManualHeadingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAligned = QiblaCalculator.isAligned(compassHeading, qiblaBearing)

    // Smooth rotation angle interpolation
    val animatedHeading by animateFloatAsState(
        targetValue = compassHeading,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compass_heading"
    )

    // Pulse animation when aligned
    val infiniteTransition = rememberInfiniteTransition(label = "aligned_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val ringGlowColor by animateColorAsState(
        targetValue = if (isAligned) QiblaGreenGlow else GoldAccent.copy(alpha = 0.3f),
        label = "glow_color"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qibla_compass_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status Badge
        Surface(
            color = if (isAligned) QiblaGreenGlow.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(50),
            border = BorderStroke(
                width = 1.5.dp,
                color = if (isAligned) QiblaGreenGlow else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isAligned) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Aligned",
                        tint = QiblaGreenGlow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "أنت باتجاه القبلة تماماً ✓",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = QiblaGreenGlow,
                            fontWeight = FontWeight.Bold
                        )
                    )
                } else {
                    val angleDiff = (qiblaBearing.toFloat() - compassHeading + 360f) % 360f
                    val turnDirection = if (angleDiff <= 180f) "أدر لليمين" else "أدر لليسار"
                    val turnAngle = if (angleDiff <= 180f) angleDiff.toInt() else (360 - angleDiff).toInt()

                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Turn",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$turnDirection $turnAngle°",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }

        // Compass Canvas
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(310.dp)
                .testTag("compass_canvas_container")
        ) {
            // Radiant glow ring behind the compass
            if (isAligned) {
                Box(
                    modifier = Modifier
                        .size((300 * pulseScale).dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    QiblaGreenGlow.copy(alpha = 0.35f),
                                    QiblaGreenGlow.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Canvas(modifier = Modifier.size(290.dp)) {
                drawCompassDial(
                    heading = animatedHeading,
                    qiblaAngle = qiblaBearing.toFloat(),
                    isAligned = isAligned,
                    ringGlowColor = ringGlowColor
                )
            }

            // Center Info Badge (Heading & Kaaba Bearing)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "${compassHeading.toInt()}°",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "القبلة: ${qiblaBearing.toInt()}°",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Distance to Makkah Information
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المسافة إلى مكة المكرمة",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = "${String.format("%,.0f", distanceKaabaKm)} كم",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Manual rotation / Simulator slider for testing in browser or when sensor is missing
        if (!isSensorAvailable) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "محاكاة تدوير الهاتف (للاختبار):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        IconButton(
                            onClick = { onManualHeadingChange(qiblaBearing.toFloat()) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Snap to Qibla",
                                tint = GoldAccent
                            )
                        }
                    }

                    Slider(
                        value = compassHeading,
                        onValueChange = onManualHeadingChange,
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isAligned) QiblaGreenGlow else GoldAccent,
                            activeTrackColor = if (isAligned) QiblaGreenGlow else GoldAccent
                        ),
                        modifier = Modifier.testTag("compass_manual_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0° (شمال)", style = MaterialTheme.typography.labelSmall)
                        Text("${compassHeading.toInt()}°", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Text("360°", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCompassDial(
    heading: Float,
    qiblaAngle: Float,
    isAligned: Boolean,
    ringGlowColor: Color
) {
    val center = Offset(size.width / 2f, size.height / 2f)
    val radius = size.minDimension / 2f - 16.dp.toPx()

    // Outer Circle Ring
    drawCircle(
        color = ringGlowColor,
        radius = radius,
        center = center,
        style = Stroke(width = if (isAligned) 4.dp.toPx() else 2.5.dp.toPx())
    )

    // Inner dial background
    drawCircle(
        color = Color(0x1A14533F),
        radius = radius - 4.dp.toPx(),
        center = center
    )

    // Degree Ticks & Cardinal Directions (Rotates with heading)
    rotate(-heading, center) {
        for (deg in 0 until 360 step 5) {
            val angleRad = Math.toRadians(deg.toDouble())
            val isMajor = deg % 30 == 0
            val isCardinal = deg % 90 == 0

            val tickLength = when {
                isCardinal -> 16.dp.toPx()
                isMajor -> 10.dp.toPx()
                else -> 5.dp.toPx()
            }

            val strokeWidth = when {
                isCardinal -> 3.dp.toPx()
                isMajor -> 2.dp.toPx()
                else -> 1.dp.toPx()
            }

            val tickColor = when {
                deg == 0 -> Color(0xFFEF4444) // North in Red
                isCardinal -> GoldAccent
                isMajor -> Color(0xFFC7D8CF)
                else -> Color(0x66C7D8CF)
            }

            val startX = center.x + (radius - tickLength) * sin(angleRad).toFloat()
            val startY = center.y - (radius - tickLength) * cos(angleRad).toFloat()
            val endX = center.x + radius * sin(angleRad).toFloat()
            val endY = center.y - radius * cos(angleRad).toFloat()

            drawLine(
                color = tickColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // True North pointer triangle (Red)
        val northPath = Path().apply {
            moveTo(center.x, center.y - radius + 18.dp.toPx())
            lineTo(center.x - 9.dp.toPx(), center.y - radius + 34.dp.toPx())
            lineTo(center.x + 9.dp.toPx(), center.y - radius + 34.dp.toPx())
            close()
        }
        drawPath(northPath, color = Color(0xFFEF4444))

        // South pointer triangle (White/Slate)
        val southPath = Path().apply {
            moveTo(center.x, center.y + radius - 18.dp.toPx())
            lineTo(center.x - 9.dp.toPx(), center.y + radius - 34.dp.toPx())
            lineTo(center.x + 9.dp.toPx(), center.y + radius - 34.dp.toPx())
            close()
        }
        drawPath(southPath, color = Color(0x88FFFFFF))

        // QIBLA POINTER & KAABA EMBLEM at qiblaAngle relative to dial!
        rotate(qiblaAngle, center) {
            val qiblaY = center.y - radius + 22.dp.toPx()

            // Glowing Golden Kaaba Cube representation
            val kaabaSize = 22.dp.toPx()
            drawRect(
                color = Color(0xFF1F1D1D), // Dark Kaaba stone
                topLeft = Offset(center.x - kaabaSize / 2, qiblaY - kaabaSize / 2),
                size = Size(kaabaSize, kaabaSize)
            )

            // Golden Kiswa band on Kaaba
            drawRect(
                color = GoldAccent,
                topLeft = Offset(center.x - kaabaSize / 2, qiblaY - kaabaSize / 2 + 4.dp.toPx()),
                size = Size(kaabaSize, 3.5.dp.toPx())
            )

            // Green/Gold pointer arrow to Kaaba
            val arrowPath = Path().apply {
                moveTo(center.x, qiblaY - 14.dp.toPx())
                lineTo(center.x - 8.dp.toPx(), qiblaY - 2.dp.toPx())
                lineTo(center.x + 8.dp.toPx(), qiblaY - 2.dp.toPx())
                close()
            }
            drawPath(
                path = arrowPath,
                color = if (isAligned) QiblaGreenGlow else GoldAccentLight
            )

            // Line from center to Kaaba
            drawLine(
                color = if (isAligned) QiblaGreenGlow.copy(alpha = 0.8f) else GoldAccent.copy(alpha = 0.5f),
                start = center,
                end = Offset(center.x, qiblaY + kaabaSize / 2),
                strokeWidth = if (isAligned) 2.5.dp.toPx() else 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
            )
        }
    }
}
