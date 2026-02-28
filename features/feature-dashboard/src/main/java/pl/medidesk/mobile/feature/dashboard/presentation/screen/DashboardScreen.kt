package pl.medidesk.mobile.feature.dashboard.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.medidesk.mobile.core.model.DashboardData
import pl.medidesk.mobile.core.ui.components.ErrorScreen
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardUiState
import pl.medidesk.mobile.feature.dashboard.presentation.viewmodel.DashboardViewModel

@Composable
fun DashboardScreen(
    eventId: String,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(eventId) { viewModel.loadDashboard(eventId) }

    when (val state = uiState) {
        is DashboardUiState.Loading -> LoadingScreen("Ładowanie dashboardu...")
        is DashboardUiState.Error -> ErrorScreen(state.message, onRetry = { viewModel.loadDashboard(eventId) })
        is DashboardUiState.Success -> DashboardContent(data = state.data)
    }
}

@Composable
private fun DashboardContent(data: DashboardData) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // KPI Cards
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Zarejestrowani", data.totalRegistered.toString(), Modifier.weight(1f))
                KpiCard("Wejścia", data.checkedIn.toString(), Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary)
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard("Walk-in", data.walkIns.toString(), Modifier.weight(1f))
                KpiCard("Frekwencja", "${"%.1f".format(data.checkInRate)}%", Modifier.weight(1f),
                    color = if (data.checkInRate >= 80) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary)
            }
        }

        // By ticket class
        if (data.byTicketClass.isNotEmpty()) {
            item { Text("Wg kategorii biletów", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(data.byTicketClass) { stat ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stat.ticketName, style = MaterialTheme.typography.bodyMedium)
                    Text("${stat.checkedIn}/${stat.total}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
                LinearProgressIndicator(
                    progress = { if (stat.total > 0) stat.checkedIn.toFloat() / stat.total else 0f },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )
            }
        }

        // Top scanners
        if (data.topScanners.isNotEmpty()) {
            item { Text("Top skanerzy", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(data.topScanners.take(5)) { scanner ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(scanner.email, style = MaterialTheme.typography.bodyMedium)
                    Text("${scanner.count}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun KpiCard(label: String, value: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified) {
    Card(modifier = modifier, elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = if (color == Color.Unspecified) MaterialTheme.colorScheme.onSurface else color)
            Text(label, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
