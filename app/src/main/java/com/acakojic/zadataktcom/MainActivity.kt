package com.acakojic.zadataktcom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.acakojic.zadataktcom.factory.MapViewModelFactory
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle
import com.acakojic.zadataktcom.ui.theme.ZadatakTcomTheme
import com.acakojic.zadataktcom.viewmodel.FavoritesScreen
import com.acakojic.zadataktcom.viewmodel.MapViewModel
import com.acakojic.zadataktcom.viewmodel.ShowVehicleInfo
import com.acakojic.zadataktcom.viewmodel.VehicleDetailDialog
import com.acakojic.zadataktcom.viewmodel.VehicleMapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZadatakTcomTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// MainScreen composable that sets up the navigation
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val customRepository =
        remember { CustomRepository(context) }

    //mapViewModel start
    val mapViewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(
            repository = customRepository, context = context
        )
    )

    var showDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }

    if (showDialog) {
        VehicleDetailDialog(selectedVehicle, viewModel = mapViewModel, onDismiss = {
            showDialog = false
            selectedVehicle = null
        })
    }

    // mapViewModel end

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Map.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Map.route) {
                VehicleMapScreen(mapViewModel) { vehicle ->
                    selectedVehicle = vehicle
                    showDialog = true
                }
            }

            composable(Screen.Favorites.route) {
                FavoritesScreen(viewModel = mapViewModel, navController = navController)
            }

            composable(
                "vehicleDetails/{vehicleID}",
                arguments = listOf(navArgument("vehicleID") { type = NavType.IntType })
            ) { backStackEntry ->
                //Retrieve the vehicleID from the navigation argument
                val vehicleID = backStackEntry.arguments?.getInt("vehicleID") ?: return@composable
                ShowVehicleInfo(vehicleID = vehicleID, viewModel = mapViewModel)
            }

        }
    }
}

//BottomNavigationBar composable that shows the navigation items
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        Screen.Map,
        Screen.List,
        Screen.Favorites
    )
    BottomNavigation(
        backgroundColor = Color.Black,
//        contentColor = Color.Black
    ) {
        val currentRoute = currentRoute(navController)
        items.forEach { screen ->
            val selected = currentRoute == screen.route
            val iconColor =
                if (selected) Color(0xFFFFA500) else Color.White //orange if selected, white if not

            BottomNavigationItem(
                icon = {
                    Image(
                        painter = painterResource(id = screen.drawableId),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp), // Adjust the size as needed
                        colorFilter = ColorFilter.tint(iconColor)
                    )
                },
                label = { }, // No label needed
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

//Defines routes/screens
sealed class Screen(val route: String, @DrawableRes val drawableId: Int) {
    object Map : Screen("map", R.drawable.nav_tab_map_icon)
    object List : Screen("list", R.drawable.nav_tab_list_icon)
    object Favorites : Screen("favorites", R.drawable.nav_tab_favorite_icon)
}

//This is helper function to get the current route
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}