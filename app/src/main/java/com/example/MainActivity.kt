package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.PrayerType
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AlertSettingsDialog
import com.example.ui.components.CityPickerDialog
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AlertsAndSettingsScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    SalatAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalatAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.setGpsLocation(loc.latitude, loc.longitude)
                    }
                }
            } catch (_: SecurityException) {
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotificationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasNotificationPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle Android system Back button: return to main Prayer Times tab
    BackHandler(enabled = uiState.currentScreen != AppScreen.PRAYER_TIMES) {
        viewModel.setScreen(AppScreen.PRAYER_TIMES)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentScreen) {
                            AppScreen.PRAYER_TIMES -> "مواقيت الصلاة"
                            AppScreen.QIBLA -> "اتجاه القبلة"
                            AppScreen.ADHKAR -> "الأذكار والتسبيح"
                            AppScreen.SETTINGS -> "التنبيهات والإعدادات"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                },
                actions = {
                    if (uiState.currentScreen == AppScreen.PRAYER_TIMES) {
                        IconButton(
                            onClick = { viewModel.toggleCityPicker(true) },
                            modifier = Modifier.testTag("appbar_city_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "Choose City",
                                tint = GoldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("main_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                // 1. Prayer Times
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.PRAYER_TIMES,
                    onClick = { viewModel.setScreen(AppScreen.PRAYER_TIMES) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.PRAYER_TIMES) Icons.Filled.AccessTimeFilled else Icons.Outlined.AccessTime,
                            contentDescription = "Prayer Times"
                        )
                    },
                    label = { Text("المواقيت", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_prayer_times")
                )

                // 2. Qibla
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.QIBLA,
                    onClick = { viewModel.setScreen(AppScreen.QIBLA) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.QIBLA) Icons.Filled.Explore else Icons.Outlined.Explore,
                            contentDescription = "Qibla"
                        )
                    },
                    label = { Text("القبلة", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_qibla")
                )

                // 3. Adhkar & Tasbih
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.ADHKAR,
                    onClick = { viewModel.setScreen(AppScreen.ADHKAR) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.ADHKAR) Icons.Filled.Fingerprint else Icons.Outlined.Fingerprint,
                            contentDescription = "Adhkar"
                        )
                    },
                    label = { Text("الأذكار", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_adhkar")
                )

                // 4. Alerts & Settings
                NavigationBarItem(
                    selected = uiState.currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.setScreen(AppScreen.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentScreen == AppScreen.SETTINGS) Icons.Filled.NotificationsActive else Icons.Outlined.Notifications,
                            contentDescription = "Alerts"
                        )
                    },
                    label = { Text("التنبيهات", fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.PRAYER_TIMES -> {
                    PrayerTimesScreen(
                        uiState = uiState,
                        onCityClick = { viewModel.toggleCityPicker(true) },
                        onToggleAdhanPreview = { viewModel.toggleAdhanAudioPreview() },
                        onAlertClick = { prayerType -> viewModel.openAlertConfig(prayerType) },
                        onToggleCheckedDone = { prayerType -> viewModel.togglePrayerDone(prayerType) },
                        onNavigate = { screen -> viewModel.setScreen(screen) }
                    )
                }

                AppScreen.QIBLA -> {
                    QiblaScreen(
                        uiState = uiState,
                        onManualHeadingChange = { heading -> viewModel.setManualCompassHeading(heading) }
                    )
                }

                AppScreen.ADHKAR -> {
                    AdhkarScreen(
                        totalTasbihCount = uiState.totalTasbihCount,
                        onTasbihTap = { viewModel.onTasbihTap() }
                    )
                }

                AppScreen.SETTINGS -> {
                    AlertsAndSettingsScreen(
                        uiState = uiState,
                        onAlertClick = { prayerType -> viewModel.openAlertConfig(prayerType) },
                        onSendTestNotification = { viewModel.sendTestNotification() },
                        onCalculationMethodChange = { method -> viewModel.setCalculationMethod(method) },
                        onJuristicMethodChange = { juristic -> viewModel.setJuristicMethod(juristic) },
                        onMinuteOffsetChange = { type, offset -> viewModel.setMinuteOffset(type, offset) },
                        onToggleAdhanPreview = { viewModel.toggleAdhanAudioPreview() }
                    )
                }
            }

            // City Picker Modal Bottom Sheet
            if (uiState.isCityPickerVisible) {
                CityPickerDialog(
                    selectedCity = uiState.selectedCity,
                    isUsingGps = uiState.isUsingGps,
                    onCitySelected = { city -> viewModel.selectCity(city) },
                    onRequestGps = {
                        val hasLocationPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasLocationPermission) {
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                            try {
                                fusedClient.lastLocation.addOnSuccessListener { loc ->
                                    if (loc != null) {
                                        viewModel.setGpsLocation(loc.latitude, loc.longitude)
                                    }
                                }
                            } catch (_: SecurityException) {
                            }
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    onDismiss = { viewModel.toggleCityPicker(false) }
                )
            }

            // Prayer Custom Alert Bottom Sheet
            uiState.alertConfigInEdit?.let { config ->
                AlertSettingsDialog(
                    config = config,
                    isAudioPlaying = uiState.isAudioPlaying,
                    onPreviewAudio = { mode -> viewModel.toggleAdhanAudioPreview(mode) },
                    onSaveConfig = { updatedConfig -> viewModel.saveAlertConfig(updatedConfig) },
                    onDismiss = { viewModel.closeAlertConfig() }
                )
            }
        }
    }
}
