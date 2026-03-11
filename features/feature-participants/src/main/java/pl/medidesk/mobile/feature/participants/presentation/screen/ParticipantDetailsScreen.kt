package pl.medidesk.mobile.feature.participants.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.* // Updated for Material 3
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.medidesk.mobile.core.model.Participant
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.feature.participants.presentation.viewmodel.ParticipantDetailsUiState
import pl.medidesk.mobile.feature.participants.presentation.viewmodel.ParticipantDetailsViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantDetailsScreen(
    participantId: Long,
    onBackClick: () -> Unit,
    viewModel: ParticipantDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(participantId) { viewModel.loadParticipant(participantId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły Uczestnika", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding), color = Color(0xFFF8F9FA)) {
            when (val state = uiState) {
                is ParticipantDetailsUiState.Loading -> LoadingScreen("Ładowanie...")
                is ParticipantDetailsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
                is ParticipantDetailsUiState.Success -> ParticipantDetailsContent(state.participant)
            }
        }
    }
}

@Composable
private fun ParticipantDetailsContent(participant: Participant) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(participant.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    participant.company?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    participant.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
                    participant.ticketName?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Status", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    DetailRow(label = "Status rejestracji", value = participant.status.orEmpty())
                    DetailRow(label = "Status obecności", value = participant.attendanceStatus.orEmpty())
                    DetailRow(label = "ID biletu (Backstage)", value = participant.backstageTicketId.orEmpty())
                    DetailRow(label = "ID zamówienia", value = participant.eventOrderId.orEmpty())
                    
                    participant.checkedInAt?.let { 
                        DetailRow(label = "Data i godzina wejścia", value = formatDateTime(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatDateTime(raw: String): String {
    if (raw.isBlank()) return "Brak danych"
    return try {
        val cleanRaw = if (raw.length >= 19) raw.substring(0, 19).replace(" ", "T") else raw
        val dt = LocalDateTime.parse(cleanRaw)
        dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    } catch (e: Exception) { raw }
}
