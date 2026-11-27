package com.ginogipsy.sanmartinoapp.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ginogipsy.sanmartinoapp.data.model.MenuKind
import com.ginogipsy.sanmartinoapp.ui.screens.cantine.CantineScreen
import com.ginogipsy.sanmartinoapp.ui.screens.events.EventsScreen
import com.ginogipsy.sanmartinoapp.ui.screens.menu.MenuScreen
import com.ginogipsy.sanmartinoapp.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val EVENTS = "events"
    const val CANTINE = "cantine?eventId={eventId}&eventName={eventName}&readOnly={readOnly}"
    const val MENU = "menu/{cantinaId}/{kind}?eventId={eventId}&eventName={eventName}&readOnly={readOnly}"

    fun cantine(
        eventId: String = "11111111-1111-1111-1111-000000002026",
        eventName: String = "",
        readOnly: Boolean = false,
    ): String {
        val encId = Uri.encode(eventId)
        val encName = Uri.encode(eventName)
        return "cantine?eventId=$encId&eventName=$encName&readOnly=$readOnly"
    }

    fun menu(
        cantinaId: String,
        kind: MenuKind,
        eventId: String = "11111111-1111-1111-1111-000000002026",
        eventName: String = "",
        readOnly: Boolean = false,
    ): String {
        val encId = Uri.encode(eventId)
        val encName = Uri.encode(eventName)
        return "menu/$cantinaId/${kind.name}?eventId=$encId&eventName=$encName&readOnly=$readOnly"
    }
}

@Composable
fun SanMartinoNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                navController.navigate(Routes.EVENTS) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }
        composable(Routes.EVENTS) {
            EventsScreen(
                onOpenCantine = { eventId, eventName, readOnly ->
                    navController.navigate(Routes.cantine(eventId, eventName, readOnly))
                },
            )
        }
        composable(
            route = Routes.CANTINE,
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                    defaultValue = "11111111-1111-1111-1111-000000002026"
                },
                navArgument("eventName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("readOnly") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStack ->
            val eventId = backStack.arguments?.getString("eventId")
                ?.takeIf { it.isNotBlank() }
                ?: "11111111-1111-1111-1111-000000002026"
            val eventName = backStack.arguments?.getString("eventName").orEmpty()
            val readOnly = backStack.arguments?.getBoolean("readOnly") ?: false
            CantineScreen(
                eventId = eventId,
                eventName = eventName,
                readOnly = readOnly,
                onBack = { navController.popBackStack() },
                onOpenMenu = { cantinaId, kind ->
                    navController.navigate(
                        Routes.menu(
                            cantinaId = cantinaId,
                            kind = kind,
                            eventId = eventId,
                            eventName = eventName,
                            readOnly = readOnly,
                        )
                    )
                },
            )
        }
        composable(
            route = Routes.MENU,
            arguments = listOf(
                navArgument("cantinaId") { type = NavType.StringType },
                navArgument("kind") { type = NavType.StringType },
                navArgument("eventId") {
                    type = NavType.StringType
                    defaultValue = "11111111-1111-1111-1111-000000002026"
                },
                navArgument("eventName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("readOnly") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStack ->
            val cantinaId = backStack.arguments?.getString("cantinaId").orEmpty()
            val kind = MenuKind.valueOf(
                backStack.arguments?.getString("kind") ?: MenuKind.FOOD.name
            )
            val eventId = backStack.arguments?.getString("eventId")
                ?.takeIf { it.isNotBlank() }
                ?: "11111111-1111-1111-1111-000000002026"
            val eventName = backStack.arguments?.getString("eventName").orEmpty()
            val readOnly = backStack.arguments?.getBoolean("readOnly") ?: false
            MenuScreen(
                cantinaId = cantinaId,
                kind = kind,
                eventId = eventId,
                eventName = eventName,
                readOnly = readOnly,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
