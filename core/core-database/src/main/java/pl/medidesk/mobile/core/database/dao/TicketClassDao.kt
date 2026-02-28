package pl.medidesk.mobile.core.database.dao

import androidx.room.*
import pl.medidesk.mobile.core.database.entities.TicketClassEntity

@Dao
interface TicketClassDao {

    @Query("SELECT * FROM ticket_classes WHERE event_id = :eventId")
    suspend fun getForEvent(eventId: String): List<TicketClassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<TicketClassEntity>)

    @Query("DELETE FROM ticket_classes WHERE event_id = :eventId")
    suspend fun deleteForEvent(eventId: String)
}
