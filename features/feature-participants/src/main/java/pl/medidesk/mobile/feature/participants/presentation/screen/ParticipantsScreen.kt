package pl.medidesk.mobile.feature.participants.presentation.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    filterType: String?,
    ticketClassId: String?,
    onBackClick: () -> Unit = {},
    onParticipantClick: (Long) -> Unit,
    viewModel: ParticipantsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(filterType, ticketClassId) {
        when (filterType) {
            "checkedIn" -> viewModel.onFilterCheckedIn(true)
            "pending" -> viewModel.onFilterCheckedIn(false)
            else -> viewModel.onFilterCheckedIn(null)
        }
        viewModel.onFilterTicketClass(ticketClassId)
    }

    if (uiState.checkinDialogParticipant != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Zapis wejścia") },
            text = { Text("Czy chcesz zameldować uczestnika ${uiState.checkinDialogParticipant?.displayName}?") },
            confirmButton = {
                Button(onClick = { viewModel.performManualCheckin(uiState.checkinDialogParticipant!!) }) {
                    Text("Tak, odznacz")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Anuluj") }
            }
        )
    }
    
    if (uiState.checkoutDialogParticipant != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialogs,
            title = { Text("Cofanie wejścia") },
            text = { Text("Czy na pewno chcesz cofnąć zameldowanie dla ${uiState.checkoutDialogParticipant?.displayName}?") },
            confirmButton = {
                Button(onClick = { viewModel.performManualCheckout(uiState.checkoutDialogParticipant!!) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Cofnij wejście")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialogs) { Text("Anuluj") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Uczestnicy", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFFF1F3F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                uiState.participants.size.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color.White)) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQuery,
                placeholder = { Text("Imię, firma, bilet, płatnik, tag...", color = Color.LightGray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFF8F9FA),
                    focusedContainerColor = Color(0xFFF8F9FA),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            // Attendance Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterCheckedIn == null,
                        onClick = { viewModel.onFilterCheckedIn(null) },
                        label = { Text("Wszyscy") },
                        colors = filterChipColors()
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterCheckedIn == true,
                        onClick = { viewModel.onFilterCheckedIn(true) },
                        label = { Text("Odznaczeni") },
                        colors = filterChipColors()
                    )
                }
                item {
                    FilterChip(
                        selected = uiState.filterCheckedIn == false,
                        onClick = { viewModel.onFilterCheckedIn(false) },
                        label = { Text("Oczekujący") },
                        colors = filterChipColors()
                    )
                }
                
                item { VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 4.dp)) }
                
                item {
                    FilterChip(
                        selected = uiState.selectedTicketClassId == null,
                        onClick = { viewModel.onFilterTicketClass(null) },
                        label = { Text("Wszystkie bilety") },
                        colors = filterChipColors()
                    )
                }
                
                items(uiState.ticketClasses) { ticketClass ->
                    FilterChip(
                        selected = uiState.selectedTicketClassId == ticketClass.ticketClassId,
                        onClick = { viewModel.onFilterTicketClass(ticketClass.ticketClassId) },
                        label = { Text(ticketClass.ticketName) },
                        colors = filterChipColors()
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh(eventId) },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredParticipants, key = { "${it.id}_${it.isWalkin}" }) { participant ->
                        ParticipantItem(
                            participant = participant, 
                            onClick = { onParticipantClick(participant.id) },
                            onStatusClick = {
                                if (participant.isCheckedIn) {
                                    viewModel.showCheckoutDialog(participant)
                                } else {
                                    viewModel.showCheckinDialog(participant)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = Color(0xFF152C5B),
    selectedLabelColor = Color.White,
    containerColor = Color(0xFFF1F3F5),
    labelColor = Color.DarkGray
)

@Composable
private fun ParticipantItem(participant: Participant, onClick: () -> Unit, onStatusClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF1F3F5))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color(0xFFE8F5E9)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initials = (participant.firstName?.take(1).orEmpty() + participant.lastName?.take(1).orEmpty()).uppercase()
                    Text(initials, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${participant.lastName.orEmpty()}, ${participant.firstName.orEmpty()}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = participant.company.orEmpty().ifEmpty { 
                        participant.buyerName?.let { "Płatnik: $it" } ?: "Indywidualny"
                    },
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Surface(
                color = Color(0xFFF1F3F5),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = participant.ticketName ?: "Standard",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            IconButton(onClick = onStatusClick) {
                if (participant.isCheckedIn) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Odznaczony",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.AccessTime,
                        contentDescription = "Oczekujący",
                        tint = Color.LightGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
