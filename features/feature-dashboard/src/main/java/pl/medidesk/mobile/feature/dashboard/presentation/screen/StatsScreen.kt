package pl.medidesk.mobile.feature.dashboard.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.medidesk.mobile.core.model.DashboardData
import pl.medidesk.mobile.core.model.SyncState
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardUiState
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    eventId: String,
    onBackClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) { viewModel.loadDashboard(eventId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier.fillMaxSize().padding(padding),
            color = Color(0xFFF8F9FA)
        ) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> LoadingScreen("Ładowanie...")
                is DashboardUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
                is DashboardUiState.Success -> StatsContent(
                    data = state.data,
                    syncState = state.syncState,
                    onSyncClick = { viewModel.triggerSync(eventId) }
                )
            }
        }
    }
}

@Composable
private fun StatsContent(data: DashboardData, syncState: SyncState, onSyncClick: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Attendance Main Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("${data.checkInRate.toInt()}%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold, color = Color(0xFF152C5B))
                    Text("uczestników zameldowanych", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    LinearProgressIndicator(
                        progress = { data.checkInRate.toFloat() / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = Color(0xFF152C5B),
                        trackColor = Color(0xFFE9ECEF)
                    )
                }
            }
        }

        // 2x2 Grid for Stats
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(data.checkedIn.toString(), "ODZNACZENI", Icons.Outlined.CheckCircle, Color(0xFFE8F5E9), Color(0xFF2E7D32), Modifier.weight(1f))
                    MiniStatCard((data.totalRegistered - data.checkedIn).toString(), "OCZEKUJĄCY", Icons.Outlined.HourglassEmpty, Color(0xFFFFF3E0), Color(0xFFF57C00), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatCard(data.totalRegistered.toString(), "ŁĄCZNIE", Icons.Default.Group, Color(0xFFE3F2FD), Color(0xFF1565C0), Modifier.weight(1f))
                    MiniStatCard(syncState.totalPending.toString(), "DO SYNC", Icons.Outlined.Sync, Color(0xFFFFEBEE), Color(0xFFC62828), Modifier.weight(1f))
                }
            }
        }

        // Sync Button
        item {
            Button(
                onClick = onSyncClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text("Synchronizuj (${syncState.totalPending} oczekujących)", fontWeight = FontWeight.Bold)
            }
        }

        // Bar Chart Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Check-iny per skaner", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(16.dp))
                    
                    data.topScanners.forEach { scanner ->
                        ScannerBarRow(scanner.email, scanner.count, data.checkedIn)
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniStatCard(value: String, label: String, icon: ImageVector, bgColor: Color, iconColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(10.dp), color = bgColor) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.padding(10.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun ScannerBarRow(label: String, count: Int, total: Int) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label.substringBefore("@"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(count.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { if (total > 0) count.toFloat() / total else 0f },
            modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF152C5B),
            trackColor = Color(0xFFF8F9FA)
        )
    }
}
