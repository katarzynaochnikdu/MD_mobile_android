package pl.medidesk.mobile.feature.events.data.repository

import pl.medidesk.mobile.core.model.EventItem
import pl.medidesk.mobile.core.network.MobileApiService
import pl.medidesk.mobile.feature.events.domain.repository.EventsRepository
import javax.inject.Inject

class EventsRepositoryImpl @Inject constructor(
    private val apiService: MobileApiService
) : EventsRepository {
    override suspend fun getEvents(): Result<List<EventItem>> = try {
        val response = apiService.getEvents()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            Result.success(body.events.map { dto ->
                EventItem(dto.eventId, dto.eventName, dto.status, dto.startDate, dto.endDate, dto.venue)
            })
        } else {
            Result.failure(Exception("Błąd pobierania eventów"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
