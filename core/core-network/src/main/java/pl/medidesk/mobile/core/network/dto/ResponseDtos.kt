package pl.medidesk.mobile.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val token: String? = null,
    val user: UserDto? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class UserDto(
    val id: Int,
    val email: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    val role: String
)

@JsonClass(generateAdapter = true)
data class EventsResponse(
    val events: List<EventDto>
)

@JsonClass(generateAdapter = true)
data class EventDto(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "event_name") val eventName: String,
    val status: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    val venue: String
)

@JsonClass(generateAdapter = true)
data class ParticipantsResponse(
    @Json(name = "event_id") val eventId: String,
    val count: Int,
    val incremental: Boolean = false,
    val participants: List<ParticipantDto>
)

@JsonClass(generateAdapter = true)
data class ParticipantDto(
    val id: Long,
    @Json(name = "backstage_ticket_id") val backstageTicketId: String,
    @Json(name = "first_name") val firstName: String?,
    @Json(name = "last_name") val lastName: String?,
    val email: String?,
    val company: String?,
    @Json(name = "ticket_class_id") val ticketClassId: String?,
    @Json(name = "ticket_name") val ticketName: String?,
    val status: String?,
    @Json(name = "attendance_status") val attendanceStatus: String?,
    @Json(name = "event_order_id") val eventOrderId: String?,
    @Json(name = "checked_in_at") val checkedInAt: String?,
    @Json(name = "is_walkin") val isWalkin: Boolean = false
)

@JsonClass(generateAdapter = true)
data class CheckinResponse(
    val success: Boolean,
    @Json(name = "already_checked_in") val alreadyCheckedIn: Boolean = false,
    @Json(name = "checked_in_at") val checkedInAt: String? = null,
    val participant: ParticipantSummaryDto? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class ParticipantSummaryDto(
    val id: Long,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    val email: String,
    val company: String,
    @Json(name = "ticket_name") val ticketName: String,
    @Json(name = "ticket_class_id") val ticketClassId: String
)

@JsonClass(generateAdapter = true)
data class CheckinSyncResponse(
    val success: Boolean,
    val total: Int,
    val synced: Int,
    val duplicates: Int,
    val errors: Int
)

@JsonClass(generateAdapter = true)
data class CheckinStatsResponse(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "total_with_qr") val totalWithQr: Int,
    @Json(name = "checked_in") val checkedIn: Int,
    @Json(name = "not_checked_in") val notCheckedIn: Int,
    val scanners: List<ScannerStatDto>
)

@JsonClass(generateAdapter = true)
data class ScannerStatDto(
    @Json(name = "scanned_by") val scannedBy: String,
    @Json(name = "scan_count") val scanCount: Int
)

@JsonClass(generateAdapter = true)
data class DashboardResponse(
    @Json(name = "event_id") val eventId: String,
    @Json(name = "total_registered") val totalRegistered: Int,
    @Json(name = "total_with_qr") val totalWithQr: Int,
    @Json(name = "checked_in") val checkedIn: Int,
    @Json(name = "walk_ins") val walkIns: Int,
    @Json(name = "check_in_rate") val checkInRate: Double,
    @Json(name = "by_ticket_class") val byTicketClass: List<TicketClassStatDto>,
    val timeline: List<TimelineEntryDto>,
    @Json(name = "top_scanners") val topScanners: List<TopScannerDto>
)

@JsonClass(generateAdapter = true)
data class TicketClassStatDto(
    @Json(name = "ticket_name") val ticketName: String,
    val total: Int,
    @Json(name = "checked_in") val checkedIn: Int
)

@JsonClass(generateAdapter = true)
data class TimelineEntryDto(
    val hour: String,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class TopScannerDto(
    val email: String,
    val count: Int
)

@JsonClass(generateAdapter = true)
data class TicketClassesResponse(
    @Json(name = "ticket_classes") val ticketClasses: List<TicketClassDto>
)

@JsonClass(generateAdapter = true)
data class TicketClassDto(
    @Json(name = "ticket_class_id") val ticketClassId: String,
    @Json(name = "ticket_name") val ticketName: String,
    @Json(name = "event_id") val eventId: String
)

@JsonClass(generateAdapter = true)
data class WalkinResponse(
    val success: Boolean,
    val id: Long? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class WalkinBatchResponse(
    val success: Boolean,
    val total: Int,
    val synced: Int,
    val duplicates: Int,
    val errors: Int
)

@JsonClass(generateAdapter = true)
data class WalkinsListResponse(
    val walkins: List<WalkinDto>
)

@JsonClass(generateAdapter = true)
data class WalkinDto(
    val id: Long,
    @Json(name = "walk_in_code") val walkInCode: String,
    @Json(name = "event_id") val eventId: String,
    @Json(name = "first_name") val firstName: String,
    @Json(name = "last_name") val lastName: String,
    val email: String?,
    val phone: String?,
    val company: String?,
    @Json(name = "ticket_class_id") val ticketClassId: String?,
    @Json(name = "ticket_name") val ticketName: String?,
    val notes: String?,
    @Json(name = "checked_in_at") val checkedInAt: String?,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "sync_status") val syncStatus: String
)

@JsonClass(generateAdapter = true)
data class InHubConfigResponse(
    val exists: Boolean,
    val id: Int? = null,
    @Json(name = "event_id") val eventId: String? = null,
    @Json(name = "auto_checkin") val autoCheckin: Boolean? = null,
    @Json(name = "show_search") val showSearch: Boolean? = null,
    @Json(name = "show_walkin") val showWalkin: Boolean? = null,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class VerifyPinResponse(
    val valid: Boolean
)

@JsonClass(generateAdapter = true)
data class GusLookupResponse(
    val success: Boolean,
    val data: GusCompanyDto? = null,
    val error: String? = null
)

@JsonClass(generateAdapter = true)
data class GusCompanyDto(
    val name: String,
    val regon: String? = null,
    val street: String? = null,
    val zip: String? = null,
    val city: String? = null,
    val voivodeship: String? = null,
    val krs: String? = null
)
