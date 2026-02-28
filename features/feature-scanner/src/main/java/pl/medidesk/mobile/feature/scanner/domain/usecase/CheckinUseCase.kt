package pl.medidesk.mobile.feature.scanner.domain.usecase

import pl.medidesk.mobile.core.model.CheckinResult
import pl.medidesk.mobile.core.network.MobileApiService
import pl.medidesk.mobile.core.network.dto.CheckinRequest
import pl.medidesk.mobile.core.database.dao.OfflineCheckinDao
import pl.medidesk.mobile.core.database.dao.ParticipantDao
import pl.medidesk.mobile.core.database.entities.OfflineCheckinEntity
import pl.medidesk.mobile.core.model.ParticipantSummary
import java.time.Instant
import javax.inject.Inject

class CheckinUseCase @Inject constructor(
    private val apiService: MobileApiService,
    private val participantDao: ParticipantDao,
    private val offlineCheckinDao: OfflineCheckinDao
) {
    suspend operator fun invoke(ticketId: String, eventId: String): CheckinResult {
        val scannedAt = Instant.now().toString()

        return try {
            val response = apiService.checkin(CheckinRequest(ticketId, eventId, scannedAt))
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success && body.checkedInAt != null) {
                    participantDao.markCheckedIn(ticketId, body.checkedInAt)
                }
                CheckinResult(
                    success = body.success,
                    alreadyCheckedIn = body.alreadyCheckedIn,
                    checkedInAt = body.checkedInAt,
                    participant = body.participant?.let {
                        ParticipantSummary(it.id, it.firstName, it.lastName, it.email, it.company, it.ticketName, it.ticketClassId)
                    },
                    error = body.error
                )
            } else {
                // Network error — try local lookup then queue
                localCheckin(ticketId, eventId, scannedAt)
            }
        } catch (e: Exception) {
            // Offline — queue for later sync
            localCheckin(ticketId, eventId, scannedAt)
        }
    }

    private suspend fun localCheckin(ticketId: String, eventId: String, scannedAt: String): CheckinResult {
        val local = participantDao.findByTicketId(ticketId)
        return if (local != null) {
            if (local.checkedInAt != null) {
                CheckinResult(success = true, alreadyCheckedIn = true, checkedInAt = local.checkedInAt,
                    participant = ParticipantSummary(local.id, local.firstName ?: "", local.lastName ?: "",
                        local.email ?: "", local.company ?: "", local.ticketName ?: "", local.ticketClassId ?: ""))
            } else {
                offlineCheckinDao.insert(OfflineCheckinEntity(backstageTicketId = ticketId, eventId = eventId, scannedAt = scannedAt))
                participantDao.markCheckedIn(ticketId, scannedAt)
                CheckinResult(success = true, alreadyCheckedIn = false, checkedInAt = scannedAt,
                    participant = ParticipantSummary(local.id, local.firstName ?: "", local.lastName ?: "",
                        local.email ?: "", local.company ?: "", local.ticketName ?: "", local.ticketClassId ?: ""))
            }
        } else {
            CheckinResult(success = false, error = "not_found")
        }
    }
}
