package pl.medidesk.mobile.feature.participants.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.medidesk.mobile.core.model.Participant
import pl.medidesk.mobile.core.ui.components.SyncStatusBar
import pl.medidesk.mobile.core.ui.theme.ScanSuccess
import pl.medidesk.mobile.feature.participants.presentation.viewmodel.ParticipantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantsScreen(
    eventId: String,
    viewModel: ParticipantsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        SyncStatusBar(syncState = uiState.syncState, isOffline = false)

        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQuery,
            placeholder = { Text("Szukaj gościa...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                Box {
                    IconButton(onClick = { showFilterMenu = true }) {
                        Icon(Icons.Default.FilterList, null)
                    }
                    DropdownMenu(expanded = showFilterMenu, onDismissRequest = { showFilterMenu = false }) {
                        DropdownMenuItem(text = { Text("Wszyscy") }, onClick = { viewModel.onFilterCheckedIn(null); showFilterMenu = false })
                        DropdownMenuItem(text = { Text("Zarejestrowani") }, onClick = { viewModel.onFilterCheckedIn(true); showFilterMenu = false })
                        DropdownMenuItem(text = { Text("Oczekujący") }, onClick = { viewModel.onFilterCheckedIn(false); showFilterMenu = false })
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            singleLine = true
        )

        // Stats row
        val total = uiState.participants.size
        val checkedIn = uiState.participants.count { it.isCheckedIn }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${uiState.filteredParticipants.size} / $total",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Wejście: $checkedIn",
                style = MaterialTheme.typography.bodyMedium,
                color = ScanSuccess)
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh(eventId) },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.filteredParticipants, key = { "${it.id}_${it.isWalkin}" }) { participant ->
                    ParticipantRow(participant = participant)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: Participant) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (participant.isCheckedIn) {
            Icon(Icons.Default.CheckCircle, null, tint = ScanSuccess, modifier = Modifier.size(20.dp))
        } else {
            Spacer(modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(participant.displayName, style = MaterialTheme.typography.bodyLarge)
            val company = participant.company
            if (!company.isNullOrBlank()) {
                Text(company, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(participant.ticketName ?: "", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary)
        }
        if (participant.isWalkin) {
            Badge { Text("W") }
        }
    }
}
