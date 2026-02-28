package pl.medidesk.mobile.feature.events.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.medidesk.mobile.core.model.EventItem
import pl.medidesk.mobile.core.ui.components.ErrorScreen
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.feature.events.presentation.viewmodel.EventsUiState
import pl.medidesk.mobile.feature.events.presentation.viewmodel.EventsViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEventSelected: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Wybierz wydarzenie") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is EventsUiState.Loading -> LoadingScreen("Pobieranie wydarzeń...")
                is EventsUiState.Error -> ErrorScreen(state.message, onRetry = viewModel::loadEvents)
                is EventsUiState.Success -> {
                    if (state.events.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Brak aktywnych wydarzeń", style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(state.events, key = { it.eventId }) { event ->
                                EventCard(event = event, onClick = { onEventSelected(event.eventId) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(raw: String): String {
    if (raw.isBlank()) return ""
    return try {
        val date = when {
            raw.length >= 19 -> LocalDateTime.parse(raw.substring(0, 19),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toLocalDate()
            raw.contains("T") -> LocalDateTime.parse(raw.substring(0, 19)).toLocalDate()
            else -> LocalDate.parse(raw.substring(0, 10))
        }
        date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    } catch (e: Exception) {
        raw.take(10)
    }
}

@Composable
private fun EventCard(event: EventItem, onClick: () -> Unit) {
    val startFormatted = formatDate(event.startDate)
    val endFormatted = formatDate(event.endDate)
    val dateLabel = when {
        startFormatted.isNotBlank() && endFormatted.isNotBlank() && startFormatted != endFormatted ->
            "$startFormatted – $endFormatted"
        startFormatted.isNotBlank() -> startFormatted
        endFormatted.isNotBlank() -> endFormatted
        else -> ""
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                event.eventName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            )
            if (event.status.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        event.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            if (dateLabel.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (event.venue.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(6.dp))
                    Text(event.venue, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
