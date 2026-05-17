package com.nammayantra.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

object NavRoutes {
    // Auth
    const val AUTH = "auth"
    const val USER_TYPE = "user_type"

    // Main (bottom nav)
    const val HOME = "home"
    const val MACHINES = "machines"
    const val BOOKINGS = "bookings"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"

    // Detail
    const val MACHINE_DETAIL = "machine_detail/{machineId}"
    const val ADD_MACHINE = "add_machine"
    const val EDIT_MACHINE = "edit_machine/{machineId}"

    // Helpers
    fun machineDetail(machineId: String) = "machine_detail/$machineId"
    fun editMachine(machineId: String) = "edit_machine/$machineId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    companion object {
        val Home = BottomNavItem(NavRoutes.HOME, "Home", Icons.Outlined.Home, Icons.Filled.Home)
        val Machines = BottomNavItem(NavRoutes.MACHINES, "Machines", Icons.Outlined.Agriculture, Icons.Filled.Agriculture)
        val Bookings = BottomNavItem(NavRoutes.BOOKINGS, "Bookings", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark)
        val Dashboard = BottomNavItem(NavRoutes.DASHBOARD, "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard)
        val Profile = BottomNavItem(NavRoutes.PROFILE, "Profile", Icons.Outlined.Person, Icons.Filled.Person)
    }
}
