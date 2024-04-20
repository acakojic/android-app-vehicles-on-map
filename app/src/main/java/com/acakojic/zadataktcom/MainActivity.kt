package com.acakojic.zadataktcom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.acakojic.zadataktcom.factory.MapViewModelFactory
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle
import com.acakojic.zadataktcom.ui.theme.ZadatakTcomTheme
import com.acakojic.zadataktcom.viewmodel.MapViewModel
import com.acakojic.zadataktcom.viewmodel.VehicleDetailDialog
import com.acakojic.zadataktcom.viewmodel.VehicleMapScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZadatakTcomTheme {
                // A surface container using the 'background' color from the theme
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
    val context = LocalContext.current // Retrieve the current Compose local context
    val customRepository = remember { CustomRepository(context) } // Initialize the repository with the context
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(
        repository = customRepository, context = context))

    var showDialog by remember { mutableStateOf(false) }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }

    if (showDialog) {
        VehicleDetailDialog(selectedVehicle, onDismiss = {
            showDialog = false
            selectedVehicle = null
        }, onFavoriteToggle = { vehicle ->
//            vehicle.isFavorite = !vehicle.isFavorite
            // Here, also update the favorite status in your repository or ViewModel
            // For example: viewModel.toggleFavorite(vehicle)
//            viewModel.toggleFavorite(vehicle)
        })
    }

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
        }
    }
}

// BottomNavigationBar composable that shows the navigation items
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
                if (selected) Color(0xFFFFA500) else Color.White // orange if selected, white if not

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

// Define your routes/screens
sealed class Screen(val route: String, @DrawableRes val drawableId: Int) {
    object Map : Screen("map", R.drawable.nav_tab_map_icon)
    object List : Screen("list", R.drawable.nav_tab_list_icon)
    object Favorites : Screen("favorites", R.drawable.nav_tab_favorite_icon)
}

// Helper function to get the current route
@Composable
fun currentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}