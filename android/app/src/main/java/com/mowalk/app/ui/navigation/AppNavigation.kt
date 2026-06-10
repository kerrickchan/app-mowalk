package com.mowalk.app.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mowalk.app.di.ViewModelFactory
import com.mowalk.app.ui.calendar.CalendarScreen
import com.mowalk.app.ui.calendar.CalendarViewModel
import com.mowalk.app.ui.dashboard.DashboardScreen
import com.mowalk.app.ui.dashboard.DashboardViewModel
import com.mowalk.app.ui.main.MainActivity
import com.mowalk.app.ui.settings.SettingsScreen
import com.mowalk.app.ui.settings.SettingsViewModel
import com.mowalk.app.ui.trends.TrendsScreen
import com.mowalk.app.ui.trends.TrendsViewModel

sealed interface NavRoute {
    data object Dashboard : NavRoute
    data object Trends : NavRoute
    data object Calendar : NavRoute
    data object Settings : NavRoute
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: NavRoute = NavRoute.Dashboard,
    viewModelFactory: ViewModelFactory = ViewModelFactory(LocalContext.current.applicationContext as Application)
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<NavRoute.Dashboard> {
            DashboardScreen(
                onSettingsClick = { navController.navigate(NavRoute.Settings) },
                onTrendsClick = { navController.navigate(NavRoute.Trends) },
                onCalendarClick = { navController.navigate(NavRoute.Calendar) },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }
        composable<NavRoute.Trends> {
            TrendsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }
        composable<NavRoute.Calendar> {
            CalendarScreen(
                onBackClick = { navController.popBackStack() },
                onDayClick = { date ->
                    navController.popBackStack()
                },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }
        composable<NavRoute.Settings> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel(factory = viewModelFactory)
            )
        }
    }
}
