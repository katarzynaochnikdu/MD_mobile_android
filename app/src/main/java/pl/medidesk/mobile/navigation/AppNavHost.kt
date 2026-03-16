package pl.medidesk.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import pl.medidesk.mobile.feature.auth.presentation.screen.LoginScreen
import pl.medidesk.mobile.feature.auth.presentation.screen.RoleSelectionScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.DashboardScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.CompaniesScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.HomeScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.OrdersScreen
import pl.medidesk.mobile.feature.dashboard.presentation.screen.StatsScreen
import pl.medidesk.mobile.feature.events.presentation.screen.EventsScreen
import pl.medidesk.mobile.feature.inhub.presentation.screen.InHubScreen
import pl.medidesk.mobile.feature.more.presentation.screen.MoreScreen
import pl.medidesk.mobile.feature.participants.presentation.screen.ParticipantDetailsScreen
import pl.medidesk.mobile.feature.participants.presentation.screen.ParticipantsScreen
import pl.medidesk.mobile.feature.scanner.presentation.screen.ScannerScreen
import pl.medidesk.mobile.feature.speakers.presentation.screen.SpeakersScreen
import pl.medidesk.mobile.feature.speakers.presentation.screen.SpeakerDetailScreen
import pl.medidesk.mobile.feature.sponsors.presentation.screen.SponsorsScreen
import pl.medidesk.mobile.feature.sponsors.presentation.screen.SponsorDetailScreen
import pl.medidesk.mobile.feature.walkin.presentation.screen.WalkinScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onRoleSelected = { role ->
                    navController.navigate(Screen.Login.createRoute(role))
                }
            )
        }

        composable(
            route = Screen.Login.route,
            arguments = Screen.Login.arguments
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: "ORGANIZER"
            LoginScreen(
                role = role,
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.RoleSelection.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEvents = { navController.navigate(Screen.Events.route) },
                onNavigateToScanner = { navController.navigate(Screen.GlobalScanner.route) },
                onNavigateToAttractions = { /* TODO */ },
                onNavigateToSettings = { /* TODO */ },
                onLogout = {
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
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
        
        composable(Screen.GlobalScanner.route) {
            ScannerScreen(eventId = "") // Empty eventId means global scanner
        }

        composable(
            route = Screen.Main.route,
            arguments = Screen.Main.arguments
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
            MainScreen(
                eventId = eventId,
                onLogout = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onBackToEvents = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainScreen(eventId: String, onLogout: () -> Unit, onBackToEvents: () -> Unit) {
    val innerNav = rememberNavController()
    val navBackStackEntry by innerNav.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple(Screen.Dashboard.createRoute(eventId), "Główna", Icons.Default.Home),
                    Triple(Screen.Scanner.createRoute(eventId), "Skaner", Icons.Default.QrCodeScanner),
                    Triple(Screen.InHub.createRoute(eventId), "InHub", Icons.Default.Tablet),
                    Triple(Screen.More.createRoute(eventId), "Więcej", Icons.Default.MoreHoriz),
                )
                
                items.forEach { (route, label, icon) ->
                    val baseRoute = route.substringBefore("/")
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentDestination?.hierarchy?.any { it.route?.startsWith(baseRoute) == true } == true,
                        onClick = {
                            innerNav.navigate(route) {
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
            startDestination = Screen.Dashboard.createRoute(eventId),
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = Screen.Dashboard.route,
                arguments = Screen.Dashboard.arguments
            ) {
                DashboardScreen(
                    eventId = eventId,
                    onNavigateToScanner = { innerNav.navigate(Screen.Scanner.createRoute(eventId)) },
                    onNavigateToParticipants = { filterType -> 
                        innerNav.navigate(Screen.Participants.createRoute(eventId, filterType)) 
                    },
                    onNavigateToInHub = { innerNav.navigate(Screen.InHub.createRoute(eventId)) },
                    onNavigateToStats = { innerNav.navigate(Screen.Stats.createRoute(eventId)) },
                    onNavigateToSpeakers = { innerNav.navigate(Screen.Speakers.createRoute(eventId)) },
                    onNavigateToSponsors = { innerNav.navigate(Screen.Sponsors.createRoute(eventId)) },
                    onNavigateToCompanies = { innerNav.navigate(Screen.Companies.createRoute(eventId, "participant")) },
                    onNavigateToOrders = { innerNav.navigate(Screen.Orders.createRoute(eventId)) },
                    onBackToEvents = onBackToEvents
                )
            }
            
            composable(
                route = Screen.Scanner.route,
                arguments = Screen.Scanner.arguments
            ) {
                ScannerScreen(eventId = eventId)
            }
            
            composable(
                route = Screen.Participants.route,
                arguments = Screen.Participants.arguments
            ) { backStackEntry ->
                val filterType = backStackEntry.arguments?.getString("filterType")
                val ticketClassId = backStackEntry.arguments?.getString("ticketClassId")
                ParticipantsScreen(
                    eventId = eventId,
                    filterType = filterType,
                    ticketClassId = ticketClassId,
                    onBackClick = { innerNav.popBackStack() },
                    onParticipantClick = { participantId -> 
                        innerNav.navigate(Screen.ParticipantDetails.createRoute(participantId))
                    }
                )
            }
            
            composable(
                route = Screen.InHub.route,
                arguments = Screen.InHub.arguments
            ) {
                InHubScreen(eventId = eventId)
            }
            
            composable(
                route = Screen.Stats.route,
                arguments = Screen.Stats.arguments
            ) {
                StatsScreen(
                    eventId = eventId,
                    onBackClick = { innerNav.popBackStack() }
                )
            }
            
            composable(
                route = Screen.More.route,
                arguments = Screen.More.arguments
            ) {
                MoreScreen(eventId = eventId, onLogout = onLogout)
            }
            
            composable(
                route = Screen.ParticipantDetails.route,
                arguments = Screen.ParticipantDetails.arguments
            ) { backStackEntry ->
                val participantId = backStackEntry.arguments?.getLong("participantId") ?: return@composable
                ParticipantDetailsScreen(
                    participantId = participantId,
                    onBackClick = { innerNav.popBackStack() }
                )
            }

            composable(
                route = Screen.Walkin.route,
                arguments = Screen.Walkin.arguments
            ) {
                WalkinScreen(eventId = eventId)
            }

            composable(
                route = Screen.Speakers.route,
                arguments = Screen.Speakers.arguments
            ) {
                SpeakersScreen(
                    onNavigateBack = { innerNav.popBackStack() },
                    onSpeakerClick = { speakerId ->
                        innerNav.navigate(Screen.SpeakerDetail.createRoute(eventId, speakerId))
                    }
                )
            }

            composable(
                route = Screen.SpeakerDetail.route,
                arguments = Screen.SpeakerDetail.arguments
            ) {
                SpeakerDetailScreen(
                    onNavigateBack = { innerNav.popBackStack() }
                )
            }

            composable(
                route = Screen.Sponsors.route,
                arguments = Screen.Sponsors.arguments
            ) {
                SponsorsScreen(
                    onNavigateBack = { innerNav.popBackStack() },
                    onSponsorClick = { eventSponsorId ->
                        innerNav.navigate(Screen.SponsorDetail.createRoute(eventId, eventSponsorId))
                    }
                )
            }

            composable(
                route = Screen.SponsorDetail.route,
                arguments = Screen.SponsorDetail.arguments
            ) {
                SponsorDetailScreen(
                    onNavigateBack = { innerNav.popBackStack() }
                )
            }

            composable(
                route = Screen.Companies.route,
                arguments = Screen.Companies.arguments
            ) { backStackEntry ->
                val role = backStackEntry.arguments?.getString("role") ?: "participant"
                val title = if (role == "sponsor") "Sponsorzy — Firmy" else "Firmy — Uczestnicy"
                CompaniesScreen(
                    eventId = eventId,
                    role = role,
                    title = title,
                    onNavigateBack = { innerNav.popBackStack() }
                )
            }

            composable(
                route = Screen.Orders.route,
                arguments = Screen.Orders.arguments
            ) {
                OrdersScreen(
                    eventId = eventId,
                    onNavigateBack = { innerNav.popBackStack() }
                )
            }
        }
    }
}
