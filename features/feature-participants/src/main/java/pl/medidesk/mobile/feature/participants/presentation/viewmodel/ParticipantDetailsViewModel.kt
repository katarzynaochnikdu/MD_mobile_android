package pl.medidesk.mobile.feature.participants.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.medidesk.mobile.core.database.dao.ParticipantDao
import pl.medidesk.mobile.core.model.Participant
import javax.inject.Inject

sealed class ParticipantDetailsUiState {
    data object Loading : ParticipantDetailsUiState()
    data class Success(val participant: Participant) : ParticipantDetailsUiState()
    data class Error(val message: String) : ParticipantDetailsUiState()
}

@HiltViewModel
class ParticipantDetailsViewModel @Inject constructor(
    private val participantDao: ParticipantDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val participantId: Long = savedStateHandle.get<Long>("participantId") ?: -1L

    private val _uiState = MutableStateFlow<ParticipantDetailsUiState>(ParticipantDetailsUiState.Loading)
    val uiState: StateFlow<ParticipantDetailsUiState> = _uiState.asStateFlow()

    fun loadParticipant(id: Long) {
        viewModelScope.launch {
            _uiState.value = ParticipantDetailsUiState.Loading
            try {
                val entity = participantDao.getParticipantById(id) // Potrzebujemy nowej metody w DAO
                if (entity != null) {
                    _uiState.value = ParticipantDetailsUiState.Success(
                        Participant(entity.id, entity.backstageTicketId, entity.firstName, entity.lastName, entity.email,
                            entity.company, entity.ticketClassId, entity.ticketName, entity.status, entity.attendanceStatus,
                            entity.eventOrderId, entity.eventId, entity.checkedInAt, entity.isWalkin)
                    )
                } else {
                    _uiState.value = ParticipantDetailsUiState.Error("Uczestnik nie znaleziony")
                }
            } catch (e: Exception) {
                _uiState.value = ParticipantDetailsUiState.Error(e.message ?: "Błąd")
            }
        }
    }
}
