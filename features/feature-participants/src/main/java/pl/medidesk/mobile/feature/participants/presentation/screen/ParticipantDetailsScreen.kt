package pl.medidesk.mobile.feature.participants.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karta uczestnika", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
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
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profil Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = Color(0xFFE8F5E9)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val initials = (participant.firstName?.take(1).orEmpty() + participant.lastName?.take(1).orEmpty()).uppercase()
                            Text(initials, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 28.sp)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(participant.displayName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(participant.company.orEmpty().ifEmpty { "Uczestnik indywidualny" }, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                    
                    if (participant.tags.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            participant.tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(tag, fontSize = 10.sp) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        ActionButton(
                            icon = Icons.Default.Phone,
                            label = "Zadzwoń",
                            containerColor = Color(0xFF1565C0),
                            enabled = !participant.phone.isNullOrBlank()
                        ) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${participant.phone}"))
                            context.startActivity(intent)
                        }
                        ActionButton(
                            icon = Icons.Default.Email,
                            label = "E-mail",
                            containerColor = Color(0xFF2E7D32),
                            enabled = !participant.email.isNullOrBlank()
                        ) {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${participant.email}"))
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }

        // Dane kontaktowe
        item {
            DetailSection(title = "KONTAKT") {
                InfoRow(label = "E-mail", value = participant.email.orEmpty(), icon = Icons.Default.AlternateEmail)
                InfoRow(label = "Telefon", value = participant.phone.orEmpty(), icon = Icons.Default.PhoneAndroid)
            }
        }

        // Rejestracja i Płatnik
        item {
            DetailSection(title = "REJESTRACJA") {
                InfoRow(label = "Typ biletu", value = participant.ticketName ?: "Standard", icon = Icons.Default.ConfirmationNumber)
                InfoRow(label = "Status", value = participant.status.orEmpty().uppercase(), icon = Icons.Default.Info)
                InfoRow(label = "ID zamówienia", value = participant.eventOrderId ?: "--", icon = Icons.Default.ShoppingCart)
                
                val bName = participant.buyerName
                if (!bName.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFFF1F3F5))
                    Spacer(Modifier.height(8.dp))
                    Text("KTO KUPIŁ BILET (Płatnik / Firma)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    InfoRow(label = "Nazwa / Firma", value = bName, icon = Icons.Default.Business)
                    val bEmail = participant.buyerEmail
                    if (!bEmail.isNullOrBlank()) {
                        InfoRow(label = "E-mail płatnika", value = bEmail, icon = Icons.Default.MailOutline)
                    }
                }
            }
        }

        // Status wejścia
        item {
            DetailSection(title = "STATUS OBECNOŚCI") {
                val isCheckedIn = participant.isCheckedIn
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(
                        if (isCheckedIn) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCheckedIn) Color(0xFF4CAF50) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isCheckedIn) "ZAMELDOWANY" else "OCZEKUJĄCY",
                        fontWeight = FontWeight.Bold,
                        color = if (isCheckedIn) Color(0xFF4CAF50) else Color.Gray
                    )
                }
                if (isCheckedIn) {
                    InfoRow(label = "Data wejścia", value = formatDateTime(participant.checkedInAt.orEmpty()), icon = Icons.Default.Schedule)
                }
            }
        }
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, containerColor: Color, enabled: Boolean, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = containerColor)
        ) {
            Icon(icon, contentDescription = label)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = if (enabled) Color.Black else Color.LightGray)
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, icon: ImageVector) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color(0xFFE9ECEF))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value.ifEmpty { "Nie podano" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatDateTime(raw: String): String {
    if (raw.isBlank()) return "Brak danych"
    return try {
        val cleanRaw = if (raw.length >= 19) raw.substring(0, 19).replace(" ", "T") else raw
        val dt = LocalDateTime.parse(cleanRaw)
        dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"))
    } catch (e: Exception) { raw }
}
