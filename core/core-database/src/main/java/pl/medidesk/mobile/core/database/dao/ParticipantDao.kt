package pl.medidesk.mobile.core.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import pl.medidesk.mobile.core.database.entities.ParticipantEntity

@Dao
interface ParticipantDao {

    @Query("SELECT * FROM participants WHERE event_id = :eventId ORDER BY last_name, first_name")
    fun getParticipantsFlow(eventId: String): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants WHERE event_id = :eventId ORDER BY last_name, first_name")
    suspend fun getParticipants(eventId: String): List<ParticipantEntity>

    @Query("SELECT * FROM participants WHERE backstage_ticket_id = :ticketId LIMIT 1")
    suspend fun findByTicketId(ticketId: String): ParticipantEntity?

    @Query("SELECT COUNT(*) FROM participants WHERE event_id = :eventId")
    suspend fun countForEvent(eventId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(participants: List<ParticipantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(participant: ParticipantEntity)

    @Query("DELETE FROM participants WHERE event_id = :eventId")
    suspend fun deleteAllForEvent(eventId: String)

    @Query("UPDATE participants SET checked_in_at = :checkedInAt, status = 'checked_in' WHERE backstage_ticket_id = :ticketId")
    suspend fun markCheckedIn(ticketId: String, checkedInAt: String)

    @Transaction
    suspend fun replaceAll(eventId: String, participants: List<ParticipantEntity>) {
        deleteAllForEvent(eventId)
        insertAll(participants)
    }
}
