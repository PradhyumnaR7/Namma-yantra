package com.nammayantra.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nammayantra.app.data.UserProfile
import com.nammayantra.app.navigation.BottomNavItem
import com.nammayantra.app.navigation.NavRoutes
import com.nammayantra.app.ui.screens.*
import com.nammayantra.app.ui.theme.*
import com.nammayantra.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NammaYantraTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val authViewModel: AuthViewModel = viewModel()
    val userProfile by authViewModel.userProfile.collectAsStateWithLifecycle()
    val isLoggedIn = authViewModel.isLoggedIn

    val navController = rememberNavController()

    val startDestination = when {
        !isLoggedIn -> NavRoutes.AUTH
        userProfile == null -> NavRoutes.AUTH
        userProfile?.userType?.isEmpty() == true -> NavRoutes.USER_TYPE
        else -> NavRoutes.HOME
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(tween(200)) },
        exitTransition = { fadeOut(tween(200)) }
    ) {
        // Auth flow
        composable(NavRoutes.AUTH) {
            AuthScreen(
                onAuthSuccess = {
                    val profile = authViewModel.userProfile.value
                    if (profile == null || profile.userType.isEmpty()) {
                        navController.navigate(NavRoutes.USER_TYPE) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    } else {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(NavRoutes.USER_TYPE) {
            UserTypeScreen(
                phone = authViewModel.userProfile.value?.phone ?: "",
                onComplete = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.USER_TYPE) { inclusive = true }
                    }
                },
                authViewModel = authViewModel
            )
        }

        // Main app with bottom nav
        composable(NavRoutes.HOME) {
            MainScaffold(
                userProfile = userProfile,
                authViewModel = authViewModel,
                startTab = NavRoutes.HOME,
                onSignOut = {
                    navController.navigate(NavRoutes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScaffold(
    userProfile: UserProfile?,
    authViewModel: AuthViewModel,
    startTab: String,
    onSignOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Machines,
        BottomNavItem.Bookings,
        BottomNavItem.Dashboard,
        BottomNavItem.Profile
    )

    // Routes where bottom nav is hidden
    val hideBottomNavRoutes = setOf(
        NavRoutes.MACHINE_DETAIL,
        NavRoutes.ADD_MACHINE,
        NavRoutes.EDIT_MACHINE
    )
    val showBottomNav = hideBottomNavRoutes.none {
        currentDestination?.route?.startsWith(it.substringBefore("{")) == true
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = Surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.iconFilled else item.iconOutlined,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                unselectedIconColor = TextHint,
                                unselectedTextColor = TextHint,
                                indicatorColor = PrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startTab,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(tween(180)) },
            exitTransition = { fadeOut(tween(180)) }
        ) {
            composable(NavRoutes.HOME) {
                HomeScreen(navController = navController, userProfile = userProfile)
            }
            composable(NavRoutes.MACHINES) {
                EquipmentListScreen(navController = navController)
            }
            composable(NavRoutes.BOOKINGS) {
                RequestsScreen(userProfile = userProfile)
            }
            composable(NavRoutes.DASHBOARD) {
                DashboardScreen(navController = navController, userProfile = userProfile)
            }
            composable(NavRoutes.PROFILE) {
                ProfileScreen(
                    userProfile = userProfile,
                    onSignOut = onSignOut,
                    authViewModel = authViewModel
                )
            }
            composable(NavRoutes.MACHINE_DETAIL) { backStack ->
                val machineId = backStack.arguments?.getString("machineId") ?: ""
                MachineDetailScreen(
                    machineId = machineId,
                    navController = navController,
                    userProfile = userProfile
                )
            }
            composable(NavRoutes.ADD_MACHINE) {
                AddMachineScreen(
                    navController = navController,
                    userProfile = userProfile
                )
            }
        }
    }
}
