package com.acakojic.zadataktcom.viewmodel

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.acakojic.zadataktcom.service.Vehicle

class FavoriteViewModel {
}


@Composable
fun FavoritesScreen(viewModel: MapViewModel) {
    // This will hold the list of favorite vehicles
    val favoriteVehicles =
        viewModel.vehicles.observeAsState().value?.filter { it.isFavorite } ?: listOf()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val horizontalPadding = screenWidth * 0.03f // 3% of screen width
    val verticalPadding = screenWidth * 0.03f // 3% for top and bottom

    LazyColumn {

        items(favoriteVehicles) { vehicle ->
            Spacer(modifier = Modifier.height(20.dp))
            VehicleCard(
                vehicle = vehicle, viewModel = viewModel,
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = verticalPadding)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color.Black)
                    .shadow(8.dp, RoundedCornerShape(10.dp))
                    .fillMaxWidth()
            )
        }
    }
}


@Composable
fun VehicleCard(vehicle: Vehicle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
//        elevation = 4.dp
    ) {
        Column {
            Image(
                painter = rememberImagePainter(vehicle.imageURL), // Replace with your image loading logic
                contentDescription = "Vehicle Image",
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = vehicle.name, style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${vehicle.price}€", style = MaterialTheme.typography.bodyMedium)
                    // Add rating and other details here
                }
                IconButton(onClick = { /* Handle favorite click */ }) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorite",
                        tint = Color.Yellow
                    )
                }
            }
        }
    }
}
