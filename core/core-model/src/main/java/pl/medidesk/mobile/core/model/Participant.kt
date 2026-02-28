package pl.medidesk.mobile.core.model

data class Participant(
    val id: Long,
    val backstageTicketId: String,
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val company: String?,
    val ticketClassId: String?,
    val ticketName: String?,
    val status: String?,
    val attendanceStatus: String?,
    val eventOrderId: String?,
    val eventId: String,
    val checkedInAt: String?,
    val isWalkin: Boolean = false
) {
    val displayName: String get() = "${firstName.orEmpty()} ${lastName.orEmpty()}".trim()
    val isCheckedIn: Boolean get() = checkedInAt != null
}
