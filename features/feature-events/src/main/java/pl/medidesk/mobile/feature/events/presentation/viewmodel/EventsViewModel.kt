package pl.medidesk.mobile.feature.events.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pl.medidesk.mobile.core.datastore.AuthDataStore
import pl.medidesk.mobile.core.model.EventItem
import pl.medidesk.mobile.feature.events.domain.repository.EventsRepository
import pl.medidesk.mobile.feature.events.presentation.screen.parseToDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

data class UiEventGroup(
    val monthYear: String,
    val events: List<EventItem>
)

data class EventsUiState(
    val isLoading: Boolean = false,
    val groupedEvents: List<UiEventGroup> = emptyList(),
    val totalActiveEvents: Int = 0,
    val error: String? = null,
    val searchQuery: String = "",
    val statusFilter: String = "Wszystkie statusy",
    val sortOrder: String = "Najbliższe"
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val eventsRepository: EventsRepository,
    private val authDataStore: AuthDataStore
) : ViewModel() {

    private val _rawEvents = MutableStateFlow<List<EventItem>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("Wszystkie statusy")
    private val _sortOrder = MutableStateFlow("Najbliższe")
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EventsUiState> = combine(
        _rawEvents, _searchQuery, _statusFilter, _sortOrder, _isLoading, _error
    ) { args: Array<Any?> ->
        val raw = args[0] as List<EventItem>
        val query = args[1] as String
        val status = args[2] as String
        val sort = args[3] as String
        val loading = args[4] as Boolean
        val err = args[5] as String?

        var filtered = raw.filter { event ->
            (query.isBlank() || event.eventName.contains(query, ignoreCase = true)) &&
            (status == "Wszystkie statusy" || event.status == status)
        }

        filtered = if (sort == "Najbliższe") {
            filtered.sortedBy { parseToDateTime(it.startDate) }
        } else {
            filtered.sortedByDescending { parseToDateTime(it.startDate) }
        }

        val grouped = filtered.groupBy { event ->
            try {
                val date = parseToDateTime(event.startDate)
                val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pl"))
                date.format(formatter).replaceFirstChar { it.uppercase() }
            } catch (e: Exception) { "Inne" }
        }.map { (month, events) -> UiEventGroup(month, events) }

        EventsUiState(loading, grouped, filtered.size, err, query, status, sort)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EventsUiState(isLoading = true))

    init { loadEvents() }

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = eventsRepository.getEvents()
            if (result.isSuccess) {
                _rawEvents.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Błąd"
            }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun onSortOrderChange(order: String) { _sortOrder.value = order }
}
