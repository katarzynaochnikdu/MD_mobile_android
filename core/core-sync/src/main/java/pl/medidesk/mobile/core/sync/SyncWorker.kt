package pl.medidesk.mobile.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull
import pl.medidesk.mobile.core.database.dao.OfflineCheckinDao
import pl.medidesk.mobile.core.database.dao.ParticipantDao
import pl.medidesk.mobile.core.database.dao.SyncMetadataDao
import pl.medidesk.mobile.core.database.dao.WalkinDao
import pl.medidesk.mobile.core.database.entities.ParticipantEntity
import pl.medidesk.mobile.core.database.entities.SyncMetadataEntity
import pl.medidesk.mobile.core.network.MobileApiService
import pl.medidesk.mobile.core.network.dto.CheckinSyncItem
import pl.medidesk.mobile.core.network.dto.CheckinSyncRequest
import pl.medidesk.mobile.core.network.dto.WalkinBatchRequest
import pl.medidesk.mobile.core.network.dto.WalkinRequest
import java.time.Instant

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: MobileApiService,
    private val participantDao: ParticipantDao,
    private val offlineCheckinDao: OfflineCheckinDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val walkinDao: WalkinDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_EVENT_ID = "event_id"
        const val WORK_NAME_PERIODIC = "md_sync_periodic"
        const val WORK_NAME_IMMEDIATE = "md_sync_immediate"

        fun periodicWorkRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<SyncWorker>(5, java.util.concurrent.TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, java.util.concurrent.TimeUnit.MINUTES)
                .build()

        fun immediateWorkRequest(eventId: String): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(workDataOf(KEY_EVENT_ID to eventId))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
    }

    override suspend fun doWork(): Result {
        val eventId = inputData.getString(KEY_EVENT_ID) ?: return Result.failure()
        return try {
            pullParticipants(eventId)
            pushCheckins(eventId)
            pushWalkins()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun pullParticipants(eventId: String) {
        val meta = syncMetadataDao.get(eventId)
        val since = meta?.lastParticipantsSync

        val response = apiService.getParticipants(eventId, since)
        if (!response.isSuccessful) return

        val body = response.body() ?: return
        val entities = body.participants.map { dto ->
            ParticipantEntity(
                id = dto.id,
                backstageTicketId = dto.backstageTicketId,
                firstName = dto.firstName,
                lastName = dto.lastName,
                email = dto.email,
                company = dto.company,
                ticketClassId = dto.ticketClassId,
                ticketName = dto.ticketName,
                status = dto.status,
                attendanceStatus = dto.attendanceStatus,
                eventOrderId = dto.eventOrderId,
                eventId = eventId,
                checkedInAt = dto.checkedInAt,
                isWalkin = dto.isWalkin
            )
        }

        if (since == null) {
            participantDao.replaceAll(eventId, entities)
        } else {
            participantDao.insertAll(entities) // upsert (REPLACE)
        }

        val now = Instant.now().toString()
        val currentMeta = syncMetadataDao.get(eventId)
        if (currentMeta == null) {
            syncMetadataDao.upsert(SyncMetadataEntity(eventId = eventId, lastParticipantsSync = now))
        } else {
            syncMetadataDao.updateParticipantsSync(eventId, now)
        }
    }

    private suspend fun pushCheckins(eventId: String) {
        val unsynced = offlineCheckinDao.getUnsynced().filter { it.eventId == eventId }
        if (unsynced.isEmpty()) return

        val items = unsynced.map { e ->
            CheckinSyncItem(
                backstageTicketId = e.backstageTicketId,
                eventId = e.eventId,
                scannedAt = e.scannedAt,
                deviceId = e.deviceId,
                action = e.action
            )
        }

        val response = apiService.syncCheckins(CheckinSyncRequest(items))
        if (response.isSuccessful) {
            offlineCheckinDao.markAllSyncedForEvent(eventId)
            syncMetadataDao.updateCheckinPush(eventId, Instant.now().toString())
        }
    }

    private suspend fun pushWalkins() {
        val pending = walkinDao.getPending()
        if (pending.isEmpty()) return

        val items = pending.map { e ->
            WalkinRequest(
                eventId = e.eventId,
                firstName = e.firstName,
                lastName = e.lastName,
                walkInCode = e.walkInCode,
                email = e.email,
                phone = e.phone,
                company = e.company,
                ticketClassId = e.ticketClassId,
                notes = e.notes,
                checkedInAt = e.checkedInAt
            )
        }

        val response = apiService.syncWalkins(WalkinBatchRequest(items))
        if (response.isSuccessful) {
            walkinDao.markAllSynced()
        }
    }
}
