package pl.medidesk.mobile.feature.dashboard.presentation.screen

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import pl.medidesk.mobile.core.model.*
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.core.ui.theme.*
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardUiState
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardViewModel
import pl.medidesk.mobile.feature.events.presentation.screen.formatDateLabel
import java.time.LocalDateTime

@Composable
fun DashboardScreen(
    eventId: String,
    onNavigateToScanner: () -> Unit,
    onNavigateToParticipants: (filterType: String?) -> Unit,
    onNavigateToInHub: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSpeakers: () -> Unit = {},
    onNavigateToSponsors: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var forceOrganizerView by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> LoadingScreen("Ładowanie...")
            is DashboardUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
            is DashboardUiState.Success -> {
                val role = state.user.role.uppercase()
                // VERY AGGRESSIVE DETECTION
                val isOrganizer = forceOrganizerView || 
                                 role.contains("ORG") || 
                                 role.contains("ADM") || 
                                 role.contains("STAFF") || 
                                 role.contains("PRAC")

                if (isOrganizer) {
                    OrganizerDashboard(
                        data = state.data,
                        syncState = state.syncState,
                        onScannerClick = onNavigateToScanner,
                        onParticipantsClick = onNavigateToParticipants,
                        onInHubClick = onNavigateToInHub,
                        onStatsClick = onNavigateToStats,
                        onSpeakersClick = onNavigateToSpeakers,
                        onSponsorsClick = onNavigateToSponsors,
                        onSyncClick = { viewModel.triggerSync(eventId) }
                    )
                } else {
                    ParticipantDashboard(
                        data = state.data,
                        user = state.user,
                        onSyncClick = { viewModel.triggerSync(eventId) },
                        onForceOrganizer = { forceOrganizerView = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizerDashboard(
    data: DashboardData,
    syncState: SyncState,
    onScannerClick: () -> Unit,
    onParticipantsClick: (filterType: String?) -> Unit,
    onInHubClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSpeakersClick: () -> Unit,
    onSponsorsClick: () -> Unit,
    onSyncClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DashboardHeader(data, "Panel Zarządzania") }
        item { 
            Column(modifier = Modifier.padding(top = 16.dp)) {
                ProgressCard(data)
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SummaryCard(data.checkedIn.toString(), "ODZNACZENI", Color(0xFF2E7D32), Modifier.weight(1f)) { onParticipantsClick("checkedIn") }
                    SummaryCard((data.totalRegistered - data.checkedIn).toString(), "OCZEKUJĄCY", Color(0xFFF57C00), Modifier.weight(1f)) { onParticipantsClick("pending") }
                    SummaryCard(data.totalRegistered.toString(), "ŁĄCZNIE", Color(0xFF1A1C1E), Modifier.weight(1f)) { onParticipantsClick(null) }
                }
            }
        }
        item {
            Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MenuButton("Skaner QR", "Szybki check-in", Icons.Default.QrCodeScanner, Color(0xFF2196F3), onScannerClick, Modifier.weight(1f))
                MenuButton("Tryb InHub", "Kiosk samoobsługowy", Icons.Default.DesktopWindows, Color(0xFFFFA000), onInHubClick, Modifier.weight(1f))
            }
        }
        item {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Zarządzanie wydarzeniem", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 16.dp))
                val items = listOf(
                    ManagementItem("Uczestnicy", Icons.Default.Group, Color(0xFF3F51B5), { onParticipantsClick(null) }),
                    ManagementItem("Firmy", Icons.Default.Business, Color(0xFF00BFA5), onSponsorsClick),
                    ManagementItem("Sponsorzy", Icons.Default.Stars, Color(0xFFE91E63), onSponsorsClick),
                    ManagementItem("Prelegenci", Icons.Default.RecordVoiceOver, Color(0xFF9C27B0), onSpeakersClick)
                )
                items.chunked(2).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        rowItems.forEach { item -> ManagementTile(item, Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
        item { SyncButton(syncState, onSyncClick) }
    }
}

@Composable
private fun ParticipantDashboard(data: DashboardData, user: User, onSyncClick: () -> Unit, onForceOrganizer: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item { DashboardHeader(data, "Mój Panel") }
        item {
            Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("MÓJ BILET", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.size(200.dp).background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp)).border(1.dp, Color(0xFFE9ECEF), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(140.dp), tint = Color(0xFF152C5B))
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(user.email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                    Text("Standard Ticket", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Błąd rozpoznania roli? Twój status to: ${user.role}", color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onForceOrganizer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f), contentColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("WYMUŚ WIDOK ORGANIZATORA", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DashboardHeader(data: DashboardData, subtitle: String) {
    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        AsyncImage(model = data.imageUrl ?: "https://placehold.co/600x400/152C5B/FFF?text=${data.eventName.take(1)}", contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
            Text(text = data.eventName.ifEmpty { "Wydarzenie" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, color = Color(0xFF00BFA5), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp), tint = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.width(6.dp))
                Text(formatDateLabel(data.startDate), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun ProgressCard(data: DashboardData) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF152C5B)), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.padding(24.dp)) {
            Column {
                Text("POSTĘP CHECK-IN", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                Text("${data.checkInRate.toInt()}%", color = Color.White, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(progress = { data.checkInRate.toFloat() / 100f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = Color.White.copy(alpha = 0.4f), trackColor = Color.White.copy(alpha = 0.1f))
            }
        }
    }
}

@Composable
private fun SummaryCard(value: String, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = color, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
        }
    }
}

@Composable
private fun MenuButton(title: String, subtitle: String, icon: ImageVector, iconColor: Color, onClick: () -> Unit, modifier: Modifier) {
    Card(onClick = onClick, modifier = modifier.height(90.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = iconColor.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(subtitle, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SyncButton(syncState: SyncState, onSyncClick: () -> Unit) {
    Button(onClick = onSyncClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp).height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black), shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, Color(0xFFE9ECEF))) {
        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text("Synchronizuj (${syncState.totalPending} oczekujących)", fontWeight = FontWeight.Bold)
    }
}

data class ManagementItem(val title: String, val icon: ImageVector, val color: Color, val onClick: () -> Unit)

@Composable
private fun ManagementTile(item: ManagementItem, modifier: Modifier) {
    Card(onClick = item.onClick, modifier = modifier.height(110.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
