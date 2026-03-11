package pl.medidesk.mobile.feature.events.presentation.screen

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import pl.medidesk.mobile.core.model.EventItem
import pl.medidesk.mobile.core.ui.components.ErrorScreen
import pl.medidesk.mobile.core.ui.components.LoadingScreen
import pl.medidesk.mobile.feature.events.presentation.viewmodel.EventsViewModel
import pl.medidesk.mobile.feature.events.presentation.viewmodel.UiEventGroup
import java.time.LocalDateTime
import java.util.Locale

enum class EventsViewType { GRID, LIST, CALENDAR }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    onEventSelected: (String) -> Unit,
    viewModel: EventsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var viewType by remember { mutableStateOf(EventsViewType.GRID) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF00BFA5))) {
                TopAppBar(
                    title = { Text("Wydarzenia", color = Color.White, fontWeight = FontWeight.Bold) },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(onClick = { viewType = EventsViewType.GRID }) { 
                                Icon(Icons.Default.GridView, null, tint = if (viewType == EventsViewType.GRID) Color.White else Color.White.copy(alpha = 0.5f)) 
                            }
                            IconButton(onClick = { viewType = EventsViewType.LIST }) { 
                                Icon(Icons.Default.List, null, tint = if (viewType == EventsViewType.LIST) Color.White else Color.White.copy(alpha = 0.5f)) 
                            }
                            IconButton(onClick = { viewType = EventsViewType.CALENDAR }) { 
                                Icon(Icons.Default.CalendarMonth, null, tint = if (viewType == EventsViewType.CALENDAR) Color.White else Color.White.copy(alpha = 0.5f))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA))) {
            if (uiState.isLoading) LoadingScreen("Pobieranie...")
            else if (uiState.error != null) ErrorScreen(uiState.error!!, onRetry = viewModel::loadEvents)
            else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                    uiState.groupedEvents.forEach { group ->
                        item { MonthHeader(group) }
                        when (viewType) {
                            EventsViewType.GRID -> {
                                val rows = group.events.chunked(2)
                                items(rows) { rowEvents ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rowEvents.forEach { event -> EventGridCard(event, { onEventSelected(event.eventId) }, Modifier.weight(1f)) }
                                        if (rowEvents.size == 1) Spacer(Modifier.weight(1f))
                                    }
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                            EventsViewType.LIST -> { items(group.events) { event -> EventListCard(event) { onEventSelected(event.eventId) }; Spacer(Modifier.height(12.dp)) } }
                            EventsViewType.CALENDAR -> { items(group.events) { event -> EventCalendarRow(event) { onEventSelected(event.eventId) }; Spacer(Modifier.height(12.dp)) } }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(group: UiEventGroup) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
        Icon(Icons.Default.CalendarToday, null, tint = Color(0xFF00BFA5), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(group.monthYear, color = Color(0xFF00BFA5), fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun EventGridCard(event: EventItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.height(210.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Column {
            AsyncImage(
                model = event.imageUrl ?: "https://placehold.co/600x400/00BFA5/FFF?text=${event.eventName.take(1)}",
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(110.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(event.eventName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, minLines = 2, color = Color(0xFF1A1C1E))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(12.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(formatDateLabel(event.startDate), fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun EventListCard(event: EventItem, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(90.dp), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(1.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = event.imageUrl ?: "https://placehold.co/600x400/00BFA5/FFF?text=${event.eventName.take(1)}", contentDescription = null, modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            Column(modifier = Modifier.padding(12.dp).weight(1f)) {
                Text(event.eventName, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, color = Color(0xFF1A1C1E))
                Text(formatDateLabel(event.startDate), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun EventCalendarRow(event: EventItem, onClick: () -> Unit) {
    val date = parseToDateTime(event.startDate)
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(50.dp)) {
            Text(date.dayOfMonth.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF00BFA5))
            Text(date.monthValue.toString(), fontSize = 10.sp, color = Color.Gray)
        }
        Spacer(Modifier.width(16.dp))
        Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.eventName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1A1C1E))
                    Text(formatDateLabel(event.startDate), fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

fun parseToDateTime(raw: String?): LocalDateTime {
    if (raw.isNullOrBlank()) return LocalDateTime.now()
    val digits = Regex("\\d+").findAll(raw).map { it.value }.toList()
    return try {
        if (digits.size >= 3) {
            val yearIdx = digits.indexOfFirst { it.length == 4 }
            val y = if (yearIdx != -1) digits[yearIdx].toInt() else 2026
            val other = digits.filterIndexed { index, _ -> index != yearIdx }
            
            val (d, m) = if (yearIdx == 0) {
                (other.getOrNull(1)?.toInt() ?: 1) to (other.getOrNull(0)?.toInt() ?: 1)
            } else {
                (other.getOrNull(0)?.toInt() ?: 1) to (other.getOrNull(1)?.toInt() ?: 1)
            }
            
            val h = if (other.size >= 3) other[2].toInt() else 0
            val min = if (other.size >= 4) other[3].toInt() else 0
            LocalDateTime.of(y, m.coerceIn(1, 12), d.coerceIn(1, 31), h.coerceIn(0, 23), min.coerceIn(0, 59))
        } else LocalDateTime.now()
    } catch (e: Exception) { LocalDateTime.now() }
}

fun formatDateLabel(raw: String?): String {
    if (raw.isNullOrBlank()) return "Brak daty"
    val dt = parseToDateTime(raw)
    val months = listOf("", "stycznia", "lutego", "marca", "kwietnia", "maja", "czerwca", "lipca", "sierpnia", "września", "października", "listopada", "grudnia")
    return try {
        val m = months[dt.monthValue]
        val time = String.format(Locale.getDefault(), "%02d:%02d", dt.hour, dt.minute)
        "${dt.dayOfMonth} $m ${dt.year}, $time"
    } catch (e: Exception) { raw ?: "Błąd daty" }
}
