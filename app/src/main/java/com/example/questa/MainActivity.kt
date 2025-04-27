package com.example.questa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.questa.ui.navigation.Destination
import com.example.questa.ui.navigation.NavGraph
import com.example.questa.ui.theme.QuestaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuestaTheme {
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navItems = listOf(
        BottomNavItem(
            title = "Home",
            route = Destination.Home.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            title = "Favorites",
            route = Destination.FavoritePolls.route,
            selectedIcon = Icons.Filled.Star,
            unselectedIcon = Icons.Outlined.Star
        ),
        BottomNavItem(
            title = "Profile",
            route = Destination.Profile.route,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person
        )
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Only show bottom bar on main screens
    val showBottomBar = currentDestination?.hierarchy?.any { destination ->
        navItems.any { item -> item.route == destination.route }
    } ?: false
    
    if (showBottomBar) {
        NavigationBar {
            navItems.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title
                        )
                    },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = {
                        if (!selected) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(Destination.Home.route) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}