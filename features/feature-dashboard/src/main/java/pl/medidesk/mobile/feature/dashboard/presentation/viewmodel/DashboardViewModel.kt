package pl.medidesk.mobile.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.medidesk.mobile.core.model.DashboardData
import pl.medidesk.mobile.core.model.ScannerStat
import pl.medidesk.mobile.core.model.TicketClassStat
import pl.medidesk.mobile.core.model.TimelineEntry
import pl.medidesk.mobile.core.network.MobileApiService
import javax.inject.Inject

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(val data: DashboardData) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val apiService: MobileApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadDashboard(eventId: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val response = apiService.getDashboard(eventId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    _uiState.value = DashboardUiState.Success(
                        DashboardData(
                            eventId = body.eventId,
                            totalRegistered = body.totalRegistered,
                            totalWithQr = body.totalWithQr,
                            checkedIn = body.checkedIn,
                            walkIns = body.walkIns,
                            checkInRate = body.checkInRate,
                            byTicketClass = body.byTicketClass.map { TicketClassStat(it.ticketName, it.total, it.checkedIn) },
                            timeline = body.timeline.map { TimelineEntry(it.hour, it.count) },
                            topScanners = body.topScanners.map { ScannerStat(it.email, it.count) }
                        )
                    )
                } else {
                    _uiState.value = DashboardUiState.Error("Błąd pobierania dashboardu")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Błąd")
            }
        }
    }
}
