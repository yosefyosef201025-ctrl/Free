package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculation.QiblaCalculator
import com.example.ui.MainUiState
import com.example.ui.components.QiblaCompassView
import com.example.ui.theme.GoldAccent

@Composable
fun QiblaScreen(
    uiState: MainUiState,
    onManualHeadingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("qibla_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mosque,
                contentDescription = "Kaaba",
                tint = GoldAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "بوصلة اتجاه القبلة المشرفة",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Compass
        QiblaCompassView(
            compassHeading = uiState.compassHeading,
            qiblaBearing = uiState.qiblaBearing,
            distanceKaabaKm = uiState.distanceKaabaKm,
            isSensorAvailable = uiState.isSensorAvailable,
            onManualHeadingChange = onManualHeadingChange
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calibration & Accuracy Instruction Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tips",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "إرشادات دقة تحديد القبلة:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• ضع الهاتف أفقياً على راحة يدك أو سطح مستوٍ بعيداً عن المعادن والمجالات المغناطيسية.\n" +
                                "• قم بتحريك الهاتف برسم حركة رقم 8 في الهواء لمعايرة مستشعر البوصلة عند الحاجة.\n" +
                                "• عند محاذاة السهم الأخضر مع الكعبة سيضيء المؤشر ويهتز الهاتف تأكيداً.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.4f
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Kaaba Location Coordinates info
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إحداثيات الكعبة المشرفة:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = "${QiblaCalculator.KAABA_LATITUDE}° N, ${QiblaCalculator.KAABA_LONGITUDE}° E",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = GoldAccent)
                )
            }
        }
    }
}
