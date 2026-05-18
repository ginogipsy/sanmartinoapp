package com.ginogipsy.sanmartinoapp.navigation

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
    const val CANTINE = "cantine"
    const val MENU = "menu/{cantinaId}/{kind}"

    fun menu(cantinaId: String, kind: MenuKind): String = "menu/$cantinaId/${kind.name}"
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
            EventsScreen(onOpenCantine = { navController.navigate(Routes.CANTINE) })
        }
        composable(Routes.CANTINE) {
            CantineScreen(
                onBack = { navController.popBackStack() },
                onOpenMenu = { cantinaId, kind ->
                    navController.navigate(Routes.menu(cantinaId, kind))
                },
            )
        }
        composable(
            route = Routes.MENU,
            arguments = listOf(
                navArgument("cantinaId") { type = NavType.StringType },
                navArgument("kind") { type = NavType.StringType },
            ),
        ) { backStack ->
            val cantinaId = backStack.arguments?.getString("cantinaId").orEmpty()
            val kind = MenuKind.valueOf(
                backStack.arguments?.getString("kind") ?: MenuKind.FOOD.name
            )
            MenuScreen(
                cantinaId = cantinaId,
                kind = kind,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
