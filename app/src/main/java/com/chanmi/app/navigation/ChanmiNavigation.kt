package com.chanmi.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import com.chanmi.app.location.LocationManager
import com.chanmi.app.ui.calendar.CalendarScreen
import com.chanmi.app.ui.nearbychurch.NearbyChurchScreen
import com.chanmi.app.ui.prayers.PrayerCategoryListScreen
import com.chanmi.app.ui.prayers.PrayerDetailScreen
import com.chanmi.app.ui.prayers.PrayerListScreen
import com.chanmi.app.ui.rosary.MysterySelectionScreen
import java.net.URLDecoder
import java.net.URLEncoder


enum class ChanmiTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    ROSARY("rosary", "묵주기도", Icons.Default.AutoAwesome),
    CALENDAR("calendar", "달력", Icons.Default.CalendarMonth),
    PRAYERS("prayers", "기도문", Icons.Default.MenuBook),
    NEARBY_CHURCH("nearby_church", "주변 성당", Icons.Default.LocationOn),
}

private val tabRoutes = ChanmiTab.entries.map { it.route }.toSet()

@Composable
fun ChanmiApp(
    widthSizeClass: WindowWidthSizeClass = WindowWidthSizeClass.Compact,
    locationManager: LocationManager? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route
    val showBottomBar = currentRoute in tabRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    ChanmiTab.entries.forEach { tab ->
                        NavigationBarItem(
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ChanmiTab.ROSARY.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ChanmiTab.ROSARY.route) {
                MysterySelectionScreen(widthSizeClass = widthSizeClass)
            }
            composable(ChanmiTab.CALENDAR.route) {
                CalendarScreen(widthSizeClass = widthSizeClass)
            }
            composable(ChanmiTab.PRAYERS.route) {
                PrayerCategoryListScreen(
                    widthSizeClass = widthSizeClass,
                    onNavigateToList = { categoryId, categoryName ->
                        val encoded = URLEncoder.encode(categoryName, "UTF-8")
                        navController.navigate("prayer_list/$categoryId/$encoded")
                    },
                    onNavigateToDetail = { prayerId ->
                        navController.navigate("prayer_detail/$prayerId")
                    }
                )
            }
            composable(
                route = "prayer_list/{categoryId}/{categoryName}",
                arguments = listOf(
                    navArgument("categoryId") { type = NavType.StringType },
                    navArgument("categoryName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
                val categoryName = URLDecoder.decode(
                    backStackEntry.arguments?.getString("categoryName") ?: "", "UTF-8"
                )
                PrayerListScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { prayerId ->
                        navController.navigate("prayer_detail/$prayerId")
                    }
                )
            }
            composable(
                route = "prayer_detail/{prayerId}",
                arguments = listOf(
                    navArgument("prayerId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val prayerId = backStackEntry.arguments?.getString("prayerId") ?: ""
                PrayerDetailScreen(
                    prayerId = prayerId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(ChanmiTab.NEARBY_CHURCH.route) {
                if (locationManager != null) {
                    NearbyChurchScreen(widthSizeClass = widthSizeClass, locationManager = locationManager)
                } else {
                    PlaceholderScreen("주변 성당")
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
    }
}
