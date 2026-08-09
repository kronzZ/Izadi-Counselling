package com.practice.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.practice.app.PracticeViewModel
import com.practice.app.data.Session
import com.practice.app.ui.theme.IzadiMist
import kotlinx.coroutines.delay

private fun NavHostController.navigateToClientsList() {
    if (!popBackStack(Destinations.Clients, inclusive = false)) {
        navigate(Destinations.Clients) {
            popUpTo(Destinations.Home) { inclusive = false }
            launchSingleTop = true
        }
    }
}

@Composable
fun PracticeApp(
    viewModel: PracticeViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val clients = viewModel.clients
    val sessions = viewModel.sessions
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1400)
        showSplash = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Always under the splash so SoftCloud never crossfades through the brand mark.
        NavHost(
            navController = navController,
            startDestination = Destinations.Home,
            modifier = Modifier
                .fillMaxSize()
                .background(IzadiMist),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
        composable(Destinations.Home) {
            HomeScreen(
                sessions = sessions,
                welcomeSmsTemplate = viewModel.welcomeSmsTemplate,
                onWelcomeSmsTemplateChange = viewModel::updateWelcomeSmsTemplate,
                onOpenClients = { navController.navigate(Destinations.Clients) },
                onOpenSessions = { navController.navigate(Destinations.Sessions) },
                onOpenPayments = { navController.navigate(Destinations.Payments) },
                onOpenSession = { session ->
                    navController.navigate(Destinations.sessionDetail(session.id))
                },
            )
        }
        composable(Destinations.Clients) {
            ClientsScreen(
                clients = clients,
                sessions = sessions,
                onBack = { navController.popBackStack() },
                onOpenClient = { client ->
                    navController.navigate(Destinations.clientDetail(client.id))
                },
                onNewClient = {
                    navController.navigate(Destinations.NewClient)
                },
                highlightClientId = viewModel.highlightClientId,
                onHighlightConsumed = viewModel::clearHighlightClientId,
            )
        }
        composable(Destinations.NewClient) {
            NewClientScreen(
                onBack = { navController.popBackStack() },
                onClientCreated = viewModel::addClient,
                onBookFirstSession = { client ->
                    navController.navigate(Destinations.bookSession(client.id)) {
                        popUpTo(Destinations.NewClient) { inclusive = true }
                    }
                },
                onSkipFirstSession = { _ ->
                    navController.navigateToClientsList()
                },
            )
        }

        composable(
            route = Destinations.ClientDetail,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
            ),
        ) { entry ->
            val clientId = entry.arguments?.getString("clientId").orEmpty()
            val client = clients.find { it.id == clientId }
            if (client == null) {
                SectionScreen(
                    title = "Client not found",
                    description = "That client isn't in the list.",
                    onBack = { navController.popBackStack() },
                )
            } else {
                ClientDetailScreen(
                    client = client,
                    onBack = { navController.popBackStack() },
                    onClientChange = viewModel::updateClient,
                    onToggleActive = { viewModel.toggleClientActive(clientId) },
                    onViewSessions = {
                        navController.navigate(Destinations.clientSessions(clientId))
                    },
                    onDeleteClient = {
                        viewModel.deleteClient(clientId)
                        navController.popBackStack()
                    },
                )
            }
        }
        composable(
            route = Destinations.ClientSessions,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
            ),
        ) { entry ->
            val clientId = entry.arguments?.getString("clientId").orEmpty()
            val client = clients.find { it.id == clientId }
            if (client == null) {
                SectionScreen(
                    title = "Client not found",
                    description = "That client isn't in the list.",
                    onBack = { navController.popBackStack() },
                )
            } else {
                ClientSessionsScreen(
                    client = client,
                    sessions = sessions,
                    onBack = { navController.popBackStack() },
                    onOpenSession = { session ->
                        navController.navigate(Destinations.sessionDetail(session.id))
                    },
                )
            }
        }
        composable(Destinations.Sessions) {
            ManageSessionsScreen(
                sessions = sessions,
                onBack = { navController.popBackStack() },
                onNewBooking = { navController.navigate(Destinations.NewBookingChoice) },
                onOpenSession = { session ->
                    navController.navigate(Destinations.sessionDetail(session.id))
                },
            )
        }
        composable(
            route = Destinations.SessionDetail,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
            ),
        ) { entry ->
            val sessionId = entry.arguments?.getString("sessionId").orEmpty()
            val session = sessions.find { it.id == sessionId }
            if (session == null) {
                SectionScreen(
                    title = "Session not found",
                    description = "That booking isn't available.",
                    onBack = { navController.popBackStack() },
                )
            } else {
                SessionDetailScreen(
                    session = session,
                    onBack = { navController.popBackStack() },
                    onSessionChange = viewModel::updateSession,
                )
            }
        }
        composable(Destinations.NewBookingChoice) {
            NewBookingChoiceScreen(
                onBack = { navController.popBackStack() },
                onNewClient = {
                    navController.navigate(Destinations.NewClient)
                },
                onExistingClient = {
                    navController.navigate(Destinations.BookingPickClient)
                },
            )
        }
        composable(Destinations.BookingPickClient) {
            ClientsScreen(
                clients = clients,
                sessions = sessions,
                title = "Existing client",
                onBack = { navController.popBackStack() },
                onOpenClient = { client ->
                    navController.navigate(Destinations.bookSession(client.id))
                },
            )
        }
        composable(
            route = Destinations.BookSession,
            arguments = listOf(
                navArgument("clientId") { type = NavType.StringType },
            ),
        ) { entry ->
            val clientId = entry.arguments?.getString("clientId").orEmpty()
            val client = clients.find { it.id == clientId }
            if (client == null) {
                SectionScreen(
                    title = "Client not found",
                    description = "That client isn't in the list.",
                    onBack = { navController.popBackStack() },
                )
            } else {
                BookSessionScreen(
                    client = client,
                    onBack = { navController.popBackStack() },
                    onSaveBooking = { date, time, durationMinutes ->
                        viewModel.addSession(
                            Session(
                                clientId = client.id,
                                clientName = client.fullName,
                                date = date,
                                time = time,
                                durationMinutes = durationMinutes,
                            ),
                        )
                        if (viewModel.highlightClientId == client.id) {
                            navController.navigateToClientsList()
                        } else {
                            navController.popBackStack(Destinations.Home, inclusive = false)
                        }
                    },
                )
            }
        }
        composable(Destinations.Payments) {
            PaymentsScreen(
                sessions = sessions,
                onBack = { navController.popBackStack() },
                onOpenSession = { session ->
                    navController.navigate(Destinations.sessionDetail(session.id))
                },
            )
        }
        }

        // Home sits underneath; splash dissolves so home eases in underneath.
        AnimatedVisibility(
            visible = showSplash,
            enter = EnterTransition.None,
            exit = fadeOut(
                animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing),
            ),
        ) {
            SplashScreen()
        }
    }
}
