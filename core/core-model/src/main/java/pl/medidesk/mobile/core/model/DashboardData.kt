package pl.medidesk.mobile.core.model

data class DashboardData(
    val eventId: String,
    val totalRegistered: Int,
    val totalWithQr: Int,
    val checkedIn: Int,
    val walkIns: Int,
    val checkInRate: Double,
    val byTicketClass: List<TicketClassStat>,
    val timeline: List<TimelineEntry>,
    val topScanners: List<ScannerStat>
)

data class TicketClassStat(
    val ticketName: String,
    val total: Int,
    val checkedIn: Int
)

data class TimelineEntry(
    val hour: String,
    val count: Int
)

data class ScannerStat(
    val email: String,
    val count: Int
)

data class CheckinStats(
    val eventId: String,
    val totalWithQr: Int,
    val checkedIn: Int,
    val notCheckedIn: Int,
    val scanners: List<ScannerStat>
)
