package pl.medidesk.mobile.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object RoleSelection : Screen("role_selection")
    data object Events : Screen("events")
    data object Main : Screen("main/{eventId}") {
        fun createRoute(eventId: String) = "main/$eventId"
    }

    // Bottom nav tabs within Main
    data object Scanner : Screen("scanner")
    data object Participants : Screen("participants")
    data object Dashboard : Screen("dashboard")
    data object Walkin : Screen("walkin")
    data object InHub : Screen("inhub")
    data object More : Screen("more")
}
