package pl.medidesk.mobile.feature.events.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.medidesk.mobile.core.model.EventItem
import pl.medidesk.mobile.feature.events.domain.repository.EventsRepository
import javax.inject.Inject

sealed class EventsUiState {
    data object Loading : EventsUiState()
    data class Success(val events: List<EventItem>) : EventsUiState()
    data class Error(val message: String) : EventsUiState()
}

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventsUiState>(EventsUiState.Loading)
    val uiState: StateFlow<EventsUiState> = _uiState.asStateFlow()

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = EventsUiState.Loading
            val result = eventsRepository.getEvents()
            _uiState.value = if (result.isSuccess) {
                EventsUiState.Success(result.getOrThrow())
            } else {
                EventsUiState.Error(result.exceptionOrNull()?.message ?: "Błąd")
            }
        }
    }
}
