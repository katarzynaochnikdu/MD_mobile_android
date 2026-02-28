package pl.medidesk.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.medidesk.mobile.feature.auth.presentation.screen.LoginScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.DashboardScreen
import pl.medidesk.mobile.feature.events.presentation.screen.EventsScreen
import pl.medidesk.mobile.feature.inhub.presentation.screen.InHubScreen
import pl.medidesk.mobile.feature.more.presentation.screen.MoreScreen
import pl.medidesk.mobile.feature.participants.presentation.screen.ParticipantsScreen
import pl.medidesk.mobile.feature.scanner.presentation.screen.ScannerScreen
import pl.medidesk.mobile.feature.walkin.presentation.screen.WalkinScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Scanner.route, "Skaner", Icons.Default.QrCodeScanner),
    BottomNavItem(Screen.Participants.route, "Goście", Icons.Default.Group),
    BottomNavItem(Screen.Dashboard.route, "Dashboard", Icons.Default.Dashboard),
    BottomNavItem(Screen.Walkin.route, "Walk-in", Icons.Default.PersonAdd),
    BottomNavItem(Screen.InHub.route, "InHub", Icons.Default.Tablet),
    BottomNavItem(Screen.More.route, "Więcej", Icons.Default.MoreHoriz),
)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Events.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Events.route) {
            EventsScreen(
                onEventSelected = { eventId ->
                    navController.navigate(Screen.Main.createRoute(eventId))
                }
            )
        }

        composable(
            route = Screen.Main.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            MainScreen(eventId = eventId, onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}

@Composable
private fun MainScreen(eventId: String, onLogout: () -> Unit) {
    val innerNav = rememberNavController()
    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            innerNav.navigate(item.route) {
                                popUpTo(innerNav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = innerNav,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Scanner.route) {
                ScannerScreen(eventId = eventId)
            }
            composable(Screen.Participants.route) {
                ParticipantsScreen(eventId = eventId)
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(eventId = eventId)
            }
            composable(Screen.Walkin.route) {
                WalkinScreen(eventId = eventId)
            }
            composable(Screen.InHub.route) {
                InHubScreen(eventId = eventId)
            }
            composable(Screen.More.route) {
                MoreScreen(eventId = eventId, onLogout = onLogout)
            }
        }
    }
}
