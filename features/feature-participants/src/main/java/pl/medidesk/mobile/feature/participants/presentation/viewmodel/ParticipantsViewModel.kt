package pl.medidesk.mobile.feature.participants.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.medidesk.mobile.core.database.dao.ParticipantDao
import pl.medidesk.mobile.core.model.Participant
import pl.medidesk.mobile.core.model.SyncState
import pl.medidesk.mobile.core.sync.SyncEngine
import javax.inject.Inject

data class ParticipantsUiState(
    val participants: List<Participant> = emptyList(),
    val filteredParticipants: List<Participant> = emptyList(),
    val searchQuery: String = "",
    val filterCheckedIn: Boolean? = null,  // null = all, true = checked-in, false = not checked-in
    val syncState: SyncState = SyncState(),
    val isRefreshing: Boolean = false
)

@HiltViewModel
class ParticipantsViewModel @Inject constructor(
    private val participantDao: ParticipantDao,
    private val syncEngine: SyncEngine,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>("eventId") ?: ""

    private val _uiState = MutableStateFlow(ParticipantsUiState())
    val uiState: StateFlow<ParticipantsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            participantDao.getParticipantsFlow(eventId).collect { entities ->
                val participants = entities.map { e ->
                    Participant(e.id, e.backstageTicketId, e.firstName, e.lastName, e.email,
                        e.company, e.ticketClassId, e.ticketName, e.status, e.attendanceStatus,
                        e.eventOrderId, e.eventId, e.checkedInAt, e.isWalkin)
                }
                val current = _uiState.value
                _uiState.value = current.copy(
                    participants = participants,
                    filteredParticipants = applyFilters(participants, current.searchQuery, current.filterCheckedIn)
                )
            }
        }
        viewModelScope.launch {
            syncEngine.syncState.collect { syncState ->
                _uiState.value = _uiState.value.copy(syncState = syncState)
            }
        }
    }

    fun onSearchQuery(query: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            searchQuery = query,
            filteredParticipants = applyFilters(current.participants, query, current.filterCheckedIn)
        )
    }

    fun onFilterCheckedIn(filter: Boolean?) {
        val current = _uiState.value
        _uiState.value = current.copy(
            filterCheckedIn = filter,
            filteredParticipants = applyFilters(current.participants, current.searchQuery, filter)
        )
    }

    fun refresh(eventId: String) {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        syncEngine.triggerImmediateSync(eventId)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    private fun applyFilters(
        all: List<Participant>,
        query: String,
        checkedInFilter: Boolean?
    ): List<Participant> {
        return all.filter { p ->
            val matchesQuery = query.isBlank() ||
                p.displayName.contains(query, ignoreCase = true) ||
                p.email?.contains(query, ignoreCase = true) == true ||
                p.company?.contains(query, ignoreCase = true) == true ||
                p.backstageTicketId.contains(query, ignoreCase = true)
            val matchesFilter = when (checkedInFilter) {
                true -> p.isCheckedIn
                false -> !p.isCheckedIn
                null -> true
            }
            matchesQuery && matchesFilter
        }
    }
}
