package dev.nura.app.thozhil.diary.ui.navigation

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nura.app.thozhil.diary.ui.components.BottomNavItem
import dev.nura.app.thozhil.diary.ui.components.CalmBottomBar
import dev.nura.app.thozhil.diary.ui.screens.AttendanceScreen
import dev.nura.app.thozhil.diary.ui.screens.BillsScreen
import dev.nura.app.thozhil.diary.ui.screens.EmployeesScreen
import dev.nura.app.thozhil.diary.ui.screens.HomeScreen
import dev.nura.app.thozhil.diary.ui.screens.LoginScreen
import dev.nura.app.thozhil.diary.ui.screens.NoteDetailScreen
import dev.nura.app.thozhil.diary.ui.screens.ProfileScreen
import dev.nura.app.thozhil.diary.ui.screens.SiteBillsScreen
import dev.nura.app.thozhil.diary.ui.screens.SiteDetailScreen
import dev.nura.app.thozhil.diary.ui.screens.SitesScreen
import dev.nura.app.thozhil.diary.ui.screens.SplashScreen
import dev.nura.app.thozhil.diary.util.SharedPrefHelper

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val EMPLOYEES = "employees"
    const val SITES = "sites"
    const val SITE_DETAIL = "sites/{siteId}"
    const val BILLS = "bills"
    const val SITE_BILLS = "sites/{siteId}/bills"
    const val ATTENDANCE = "attendance"
    const val PROFILE = "profile"

    const val NOTE_DETAIL = "notes/{noteId}"

    fun siteDetail(siteId: Long) = "sites/$siteId"
    fun siteBills(siteId: Long) = "sites/$siteId/bills"
    fun noteDetail(noteId: Long) = "notes/$noteId"
}

private val tabs = listOf(
    BottomNavItem(NavRoutes.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    BottomNavItem(NavRoutes.EMPLOYEES, "Employees", Icons.Outlined.Groups, Icons.Filled.Groups),
    BottomNavItem(NavRoutes.SITES, "Sites", Icons.Outlined.Apartment, Icons.Filled.Apartment),
    BottomNavItem(NavRoutes.BILLS, "Bills", Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong)
)

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(NavRoutes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(
    sharedPrefHelper: SharedPrefHelper
) {
    val navController = rememberNavController()
    var userName by remember { mutableStateOf(sharedPrefHelper.getUserName()) }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (tabs.any { it.route == currentRoute }) {
                CalmBottomBar(
                    items = tabs,
                    currentRoute = currentRoute,
                    onSelect = { navController.openTab(it.route) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.SPLASH,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(NavRoutes.SPLASH) {
                SplashScreen(
                    sharedPrefHelper = sharedPrefHelper,
                    onNavigateToLogin = {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.LOGIN) {
                LoginScreen(
                    onSaveName = { name ->
                        sharedPrefHelper.saveUserName(name)
                        userName = sharedPrefHelper.getUserName()
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.HOME) {
                HomeScreen(
                    userName = userName,
                    onOpenProfile = { navController.navigate(NavRoutes.PROFILE) { launchSingleTop = true } },
                    onOpenAttendance = { navController.navigate(NavRoutes.ATTENDANCE) { launchSingleTop = true } },
                    onOpenSites = { navController.openTab(NavRoutes.SITES) },
                    onOpenSite = { navController.navigate(NavRoutes.siteDetail(it)) }
                )
            }

            composable(NavRoutes.EMPLOYEES) {
                EmployeesScreen()
            }

            composable(NavRoutes.SITES) {
                SitesScreen(onOpenSite = { navController.navigate(NavRoutes.siteDetail(it)) })
            }

            composable(
                NavRoutes.SITE_DETAIL,
                arguments = listOf(navArgument("siteId") { type = NavType.LongType })
            ) { entry ->
                SiteDetailScreen(
                    siteId = entry.arguments?.getLong("siteId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onOpenNote = { navController.navigate(NavRoutes.noteDetail(it)) }
                )
            }

            composable(
                NavRoutes.NOTE_DETAIL,
                arguments = listOf(navArgument("noteId") { type = NavType.LongType })
            ) {
                NoteDetailScreen(onBack = { navController.popBackStack() })
            }

            composable(NavRoutes.ATTENDANCE) {
                AttendanceScreen(
                    onBack = { navController.popBackStack() },
                    onOpenEmployees = { navController.openTab(NavRoutes.EMPLOYEES) }
                )
            }

            composable(NavRoutes.BILLS) {
                BillsScreen(onOpenSiteBills = { navController.navigate(NavRoutes.siteBills(it)) })
            }

            composable(
                NavRoutes.SITE_BILLS,
                arguments = listOf(navArgument("siteId") { type = NavType.LongType })
            ) { entry ->
                SiteBillsScreen(
                    siteId = entry.arguments?.getLong("siteId") ?: 0L,
                    onBack = { navController.popBackStack() },
                    // A bill opens in the same screen as a note; it shows rates and amounts
                    onOpenBill = { navController.navigate(NavRoutes.noteDetail(it)) }
                )
            }

            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    userName = userName,
                    startedOn = sharedPrefHelper.getStartedOn(),
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        sharedPrefHelper.clearUserName()
                        userName = ""
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
