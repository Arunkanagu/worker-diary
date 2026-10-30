package dev.nura.app.thozhil.diary.ui.navigation

import android.widget.Toast
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.nura.app.thozhil.diary.R
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import dev.nura.app.thozhil.diary.data.AccountRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import dev.nura.app.thozhil.diary.ui.components.BottomNavItem
import dev.nura.app.thozhil.diary.ui.components.CalmBottomBar
import dev.nura.app.thozhil.diary.ui.components.CalmNavRail
import dev.nura.app.thozhil.diary.ui.screens.AttendanceReportScreen
import dev.nura.app.thozhil.diary.ui.screens.AttendanceScreen
import dev.nura.app.thozhil.diary.ui.screens.BillsScreen
import dev.nura.app.thozhil.diary.ui.screens.EditProfileScreen
import dev.nura.app.thozhil.diary.ui.screens.EmployeesScreen
import dev.nura.app.thozhil.diary.ui.screens.ForgotPasswordScreen
import dev.nura.app.thozhil.diary.ui.screens.HomeScreen
import dev.nura.app.thozhil.diary.ui.screens.LoginScreen
import dev.nura.app.thozhil.diary.ui.screens.NoteDetailScreen
import dev.nura.app.thozhil.diary.ui.screens.PdfViewerScreen
import dev.nura.app.thozhil.diary.ui.screens.ProfileScreen
import dev.nura.app.thozhil.diary.ui.screens.SiteBillsScreen
import dev.nura.app.thozhil.diary.ui.screens.SiteDetailScreen
import dev.nura.app.thozhil.diary.ui.screens.SignUpScreen
import dev.nura.app.thozhil.diary.ui.screens.SitesScreen
import dev.nura.app.thozhil.diary.ui.screens.SplashScreen
import dev.nura.app.thozhil.diary.util.Language
import dev.nura.app.thozhil.diary.util.ProvideAppLanguage
import dev.nura.app.thozhil.diary.util.SharedPrefHelper

object NavRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"
    const val HOME = "home"
    const val EMPLOYEES = "employees"
    const val SITES = "sites"
    const val SITE_DETAIL = "sites/{siteId}"
    const val BILLS = "bills"
    const val SITE_BILLS = "sites/{siteId}/bills"
    const val ATTENDANCE = "attendance"
    const val ATTENDANCE_REPORT = "attendance/report"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "profile/edit"

    const val NOTE_DETAIL = "notes/{noteId}"
    const val PDF = "pdf?path={path}&noteId={noteId}"

    fun siteDetail(siteId: String) = "sites/$siteId"
    fun siteBills(siteId: String) = "sites/$siteId/bills"
    fun noteDetail(noteId: String) = "notes/$noteId"
    fun pdf(path: String, noteId: String) = "pdf?path=${android.net.Uri.encode(path)}&noteId=$noteId"
}

private val tabs = listOf(
    BottomNavItem(NavRoutes.HOME, R.string.home, Icons.Outlined.Home, Icons.Filled.Home),
    BottomNavItem(NavRoutes.EMPLOYEES, R.string.employees, Icons.Outlined.Groups, Icons.Filled.Groups),
    BottomNavItem(NavRoutes.SITES, R.string.sites, Icons.Outlined.Apartment, Icons.Filled.Apartment),
    BottomNavItem(NavRoutes.BILLS, R.string.bills, Icons.AutoMirrored.Outlined.ReceiptLong, Icons.AutoMirrored.Filled.ReceiptLong),
    BottomNavItem(NavRoutes.PROFILE, R.string.profile, Icons.Outlined.Person, Icons.Filled.Person)
)

private fun NavHostController.openTab(route: String) {
    navigate(route) {
        popUpTo(NavRoutes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

// Page motion: short and quiet. Moving between tabs only cross-fades, since they sit side by side;
// opening a page slides it in from the end edge a little, and going back reverses that.
private val EaseOut = CubicBezierEasing(0.2f, 0f, 0f, 1f)
private const val SlideMillis = 280
private const val FadeMillis = 180

/** Tab to tab, or out of the splash and sign-in: a plain cross-fade, no slide. */
private val AnimatedContentTransitionScope<NavBackStackEntry>.betweenTabs: Boolean
    get() = initialState.destination.route in routesWithoutNav ||
        (tabs.any { it.route == initialState.destination.route } && tabs.any { it.route == targetState.destination.route })

private val pageEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (betweenTabs) fadeIn(tween(FadeMillis))
    else slideInHorizontally(tween(SlideMillis, easing = EaseOut)) { it / 10 } + fadeIn(tween(FadeMillis))
}
private val pageExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (betweenTabs) fadeOut(tween(FadeMillis / 2))
    else slideOutHorizontally(tween(SlideMillis, easing = EaseOut)) { -it / 20 } + fadeOut(tween(FadeMillis))
}
private val pagePopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    if (betweenTabs) fadeIn(tween(FadeMillis))
    else slideInHorizontally(tween(SlideMillis, easing = EaseOut)) { -it / 20 } + fadeIn(tween(FadeMillis))
}
private val pagePopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    if (betweenTabs) fadeOut(tween(FadeMillis / 2))
    else slideOutHorizontally(tween(SlideMillis, easing = EaseOut)) { it / 10 } + fadeOut(tween(FadeMillis))
}

/** From this width (a tablet, either way up) the side rail shows on every page, not just the tabs. */
private val NavRailMinWidth = 600.dp

/** Before the app is set up there is nowhere to go, so these show no navigation. */
private val routesWithoutNav = setOf(NavRoutes.SPLASH, NavRoutes.LOGIN, NavRoutes.SIGN_UP, NavRoutes.FORGOT_PASSWORD)

@Composable
fun AppNavigation(
    sharedPrefHelper: SharedPrefHelper,
    accounts: AccountRepository
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    // The logged-in user, kept current from the users table
    val user by accounts.currentUser.collectAsState(initial = null)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val onTab = tabs.any { it.route == currentRoute }
    // The section an inner page belongs to is the nearest tab under it: a bill opened from Bills keeps Bills lit
    val backStack by navController.currentBackStack.collectAsState()
    val sectionRoute = backStack.lastOrNull { entry -> tabs.any { it.route == entry.destination.route } }?.destination?.route
    // Tablets get the side rail on every page; phones keep the bottom dock on the tabs
    val tablet = LocalConfiguration.current.screenWidthDp.dp >= NavRailMinWidth
    val showRail = tablet && currentRoute != null && currentRoute !in routesWithoutNav

    fun selectTab(item: BottomNavItem) {
        // Choosing the section you're already in goes back to its first page
        if (item.route == sectionRoute && !onTab) navController.popBackStack(item.route, inclusive = false)
        else navController.openTab(item.route)
    }

    ProvideAppLanguage {
        Row(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
        if (showRail) {
            CalmNavRail(
                items = tabs,
                currentRoute = sectionRoute,
                onSelect = ::selectTab
            )
        }
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!tablet && onTab) {
                    CalmBottomBar(
                        items = tabs,
                        currentRoute = currentRoute,
                        onSelect = ::selectTab
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SPLASH,
                enterTransition = pageEnter,
                exitTransition = pageExit,
                popEnterTransition = pagePopEnter,
                popExitTransition = pagePopExit,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                composable(NavRoutes.SPLASH) {
                    SplashScreen(
                        isLoggedIn = accounts::isLoggedIn,
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
                        onLogin = { identifier, password ->
                            accounts.login(identifier, password).also { ok ->
                                if (ok) {
                                    navController.navigate(NavRoutes.HOME) {
                                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                                    }
                                }
                            }
                        },
                        onSignUp = { navController.navigate(NavRoutes.SIGN_UP) { launchSingleTop = true } },
                        onForgotPassword = { navController.navigate(NavRoutes.FORGOT_PASSWORD) { launchSingleTop = true } },
                        onLanguageChange = {
                            sharedPrefHelper.saveLanguage(it)
                            Language.current = it
                        }
                    )
                }

                composable(NavRoutes.SIGN_UP) {
                    val accountExists by produceState(initialValue = false) { value = accounts.hasAccount() }
                    SignUpScreen(
                        accountExists = accountExists,
                        onSignUp = { name, mobile, email, password ->
                            scope.launch {
                                accounts.createAccount(name, mobile, email, password)
                                navController.navigate(NavRoutes.HOME) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.FORGOT_PASSWORD) {
                    val passwordChanged = stringResource(R.string.password_changed)
                    val context = LocalContext.current
                    ForgotPasswordScreen(
                        findAccount = { accounts.find(it) != null },
                        onReset = { identifier, password ->
                            scope.launch {
                                accounts.resetPassword(identifier, password)
                                Toast.makeText(context, passwordChanged, Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.HOME) {
                    HomeScreen(
                        userName = user?.name.orEmpty(),
                        photoPath = user?.photoPath.orEmpty(),
                        onOpenProfile = { navController.openTab(NavRoutes.PROFILE) },
                        onOpenAttendance = { navController.navigate(NavRoutes.ATTENDANCE) { launchSingleTop = true } },
                        onOpenSites = { navController.openTab(NavRoutes.SITES) },
                        onOpenSite = { navController.navigate(NavRoutes.siteDetail(it)) },
                        onOpenBills = { navController.openTab(NavRoutes.BILLS) },
                        onOpenBill = { navController.navigate(NavRoutes.noteDetail(it)) }
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
                    arguments = listOf(navArgument("siteId") { type = NavType.StringType })
                ) { entry ->
                    SiteDetailScreen(
                        siteId = entry.arguments?.getString("siteId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        onOpenNote = { navController.navigate(NavRoutes.noteDetail(it)) }
                    )
                }

                composable(
                    NavRoutes.NOTE_DETAIL,
                    arguments = listOf(navArgument("noteId") { type = NavType.StringType })
                ) {
                    NoteDetailScreen(
                        onBack = { navController.popBackStack() },
                        onOpenPdf = { path, noteId -> navController.navigate(NavRoutes.pdf(path, noteId)) }
                    )
                }

                composable(
                    NavRoutes.PDF,
                    arguments = listOf(
                        navArgument("path") { type = NavType.StringType },
                        // The bill or note the PDF was made from, so the preview can remake it in another style
                        navArgument("noteId") { type = NavType.StringType }
                    )
                ) { entry ->
                    PdfViewerScreen(
                        path = entry.arguments?.getString("path").orEmpty(),
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.ATTENDANCE) {
                    AttendanceScreen(
                        onBack = { navController.popBackStack() },
                        onOpenEmployees = { navController.openTab(NavRoutes.EMPLOYEES) },
                        onOpenReport = { navController.navigate(NavRoutes.ATTENDANCE_REPORT) { launchSingleTop = true } }
                    )
                }

                composable(NavRoutes.EDIT_PROFILE) {
                    // The form fills once the user has loaded, which is all but instant
                    user?.let { current ->
                        EditProfileScreen(
                            profile = current,
                            onSave = {
                                scope.launch {
                                    accounts.saveProfile(it)
                                    navController.popBackStack()
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }
                }

                composable(NavRoutes.ATTENDANCE_REPORT) {
                    AttendanceReportScreen(onBack = { navController.popBackStack() })
                }

                composable(NavRoutes.BILLS) {
                    BillsScreen(onOpenSiteBills = { navController.navigate(NavRoutes.siteBills(it)) })
                }

                composable(
                    NavRoutes.SITE_BILLS,
                    arguments = listOf(navArgument("siteId") { type = NavType.StringType })
                ) { entry ->
                    SiteBillsScreen(
                        siteId = entry.arguments?.getString("siteId").orEmpty(),
                        onBack = { navController.popBackStack() },
                        // A bill opens in the same screen as a note; it shows rates and amounts
                        onOpenBill = { navController.navigate(NavRoutes.noteDetail(it)) }
                    )
                }

                composable(NavRoutes.PROFILE) {
                    val current = user ?: return@composable
                    ProfileScreen(
                        profile = current,
                        // The diary was started when the account was made
                        startedOn = Instant.ofEpochMilli(current.createdAt).atZone(ZoneId.systemDefault()).toLocalDate(),
                        onEdit = { navController.navigate(NavRoutes.EDIT_PROFILE) { launchSingleTop = true } },
                        onLanguageChange = {
                            sharedPrefHelper.saveLanguage(it)
                            Language.current = it
                        },
                        onLogout = {
                            scope.launch {
                                accounts.logout()
                                navController.navigate(NavRoutes.LOGIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
}



