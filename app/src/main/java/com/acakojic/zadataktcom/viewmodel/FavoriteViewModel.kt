package com.acakojic.zadataktcom.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.acakojic.zadataktcom.data.Vehicle
import com.acakojic.zadataktcom.service.CustomRepository
import kotlinx.coroutines.launch

class FavoriteViewModel() {
}

@Composable
fun FavoritesScreen(viewModel: MapViewModel, navController: NavHostController) {
    //tthis will hold the list of favorite vehicles
    val favoriteVehicles =
        viewModel.allVehicles.observeAsState().value?.filter { it.isFavorite } ?: listOf()

    ShowVehiclesInList(
        vehicles = favoriteVehicles, viewModel = viewModel, navController = navController,
        elementOnTopOfScreen = { CenteredText("Omiljena vozila") })
}

@Composable
fun ShowVehiclesInList(
    vehicles: List<Vehicle>,
    viewModel: MapViewModel,
    navController: NavController,
    elementOnTopOfScreen: @Composable () -> Unit
) {

    Column {

        elementOnTopOfScreen()

        LazyColumn {

            items(vehicles) { vehicle ->
                Spacer(modifier = Modifier.height(20.dp))
                VehicleCard(
                    vehicle = vehicle,
                    viewModel = viewModel,
                    modifier = getModifierForFavorite(),
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun ShowVehicleInfo(vehicleID: Int, viewModel: MapViewModel) {
    val context = LocalContext.current
    val (vehicle, setVehicle) = remember { mutableStateOf<Vehicle?>(null) }
    val (isLoading, setLoading) = remember { mutableStateOf(true) }
    val repository = remember { CustomRepository(context) }

    LaunchedEffect(vehicleID) {
        viewModel.viewModelScope.launch {
            setLoading(true)
            try {
                val response =
                    repository.getVehicleDetails(context = context, vehicleID = vehicleID)
                if (response.isSuccessful) {
                    setVehicle(response.body())
                } else {
                    //todo error message
                }
            } finally {
                setLoading(false)
            }
        }
    }

    if (isLoading) {
        CircularProgressIndicator() //loading indicator while fetching data
    } else {
        vehicle?.let { vehicleDetails ->
            //Display the vehicle details
            ShowVehicleDescription(
                vehicleDetails = vehicleDetails,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ShowVehicleDescription(
    vehicleDetails: Vehicle,
    viewModel: MapViewModel
) {

    Column(
        modifier = Modifier
            .background(Color.Black),
    ) {

        CustomTopRow(title = "Vozilo")

        Spacer(modifier = Modifier.height(20.dp))

        Column(modifier = getModifierForFavorite()) {

            VehicleImageWithFavorite(
                vehicle = vehicleDetails,
                viewModel = viewModel
            )

            Spacer(modifier = Modifier.height(22.dp))

            //Model
            CustomRow(
                text = { Text(text = "Model:") },
                value = { Text(text = vehicleDetails.name) },
                icon = null
            )

            //Rating
            CustomRow(
                text = { Text(text = "Rating:") },
                value = { Text(text = vehicleDetails.rating.toString()) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating Star",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                })
            //cena
            CustomRow(
                text = { Text(text = "Cena:") },
                value = { Text(text = vehicleDetails.price.toString()) },
                icon = null
            )
            //Latitude
            CustomRow(
                text = { Text(text = "Latitude:") },
                value = { Text(text = vehicleDetails.location.latitude.toString()) },
                icon = null
            )
            //Longitude
            CustomRow(
                text = { Text(text = "Longitude:") },
                value = { Text(text = vehicleDetails.location.longitude.toString()) },
                icon = null
            )
        }
    }
}

@Composable
fun getModifierForFavorite(): Modifier {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = screenWidth * 0.03f // 3% of screen width
    val verticalPadding = screenWidth * 0.03f // 3% for top and bottom

    return Modifier
        .padding(horizontal = horizontalPadding, vertical = verticalPadding)
        .clip(RoundedCornerShape(9.dp))
        .background(Color.Black)
        .shadow(8.dp, RoundedCornerShape(10.dp))
        .fillMaxWidth()
}

@Composable
fun CustomRow(
    text: @Composable () -> Unit,
    value: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        text()
        Spacer(Modifier.weight(1f))
        value()
        icon?.invoke()
    }
    Spacer(modifier = Modifier.height(22.dp))
}

@Composable
fun CenteredText(text: String) {
//    Box(
//        modifier = Modifier
//            .fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(text = text)
//    }
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Add some padding around the text
        textAlign = TextAlign.Center, // Center the text horizontally
        style = MaterialTheme.typography.bodyLarge // Apply a text style, optional
    )
}

@Composable
fun CustomTopRow(title: String) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Back icon",
            modifier = Modifier.size(33.dp),
            tint = Color(0xFFFFA500)
        )

        Text(
            text = "Back",
            modifier = Modifier
                .clickable { backDispatcher?.onBackPressed() },
//                .padding(16.dp),
            color = Color(0xFFFFA500)
        )

        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Invisible box to balance the title
        Box(
            modifier = Modifier
                .padding(6.dp)
                .sizeIn(minWidth = 64.dp, minHeight = 48.dp)
        )
    }
}