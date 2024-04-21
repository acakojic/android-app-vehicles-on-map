package com.acakojic.zadataktcom.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.compose.rememberImagePainter
import com.acakojic.zadataktcom.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory


class MapViewModel(private val customRepository: CustomRepository, private val context: Context) :
    ViewModel() {
    private val _vehicles = MutableLiveData<List<Vehicle>?>() // This can be null
    val vehicles: LiveData<List<Vehicle>?> = _vehicles // Match the type with MutableLiveData

    init {
        viewModelScope.launch {
            try {
                val response = customRepository.getAllVehicles(context)
                if (response.isSuccessful && response.body() != null) {
                    _vehicles.value = response.body()!!
                    Log.d("MapViewModel", "success! ${vehicles.value} ")
                    Log.d("MapViewModel", "success! ${vehicles.value?.size} ")
                    Log.d("MapViewModel", "success! ${vehicles.value?.get(0)?.name} ")
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Error fetching vehicles", e)
            }
        }
    }

    fun toggleFavorite(vehicleId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            val updatedList = _vehicles.value?.map { vehicle ->
                if (vehicle.vehicleID == vehicleId) vehicle.copy(isFavorite = isFavorite) else vehicle
            }
            _vehicles.value = updatedList
        }
    }
}

@Composable
fun VehicleMapScreen(viewModel: MapViewModel, onVehicleClick: (Vehicle) -> Unit) {
    val vehicles by viewModel.vehicles.observeAsState(initial = listOf())

    val context = LocalContext.current
    val mapView = remember { initializeMap(context) }

    LaunchedEffect(vehicles) {
        mapView.overlays.clear()
        vehicles?.let {
            addMarkersToMap(
                context = context,
                mapView = mapView,
                vehicles = it,
                onVehicleClick = onVehicleClick
            )
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView })
}

@Composable
fun VehicleImageWithFavorite(
    vehicle: Vehicle,
    viewModel: MapViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { CustomRepository(context) }
    val isFavorite = remember { mutableStateOf(vehicle.isFavorite) }

    Box(contentAlignment = Alignment.TopEnd) {

        Image(
            painter = rememberImagePainter(
                data = vehicle.imageURL,
                builder = {
                    crossfade(true)
                }
            ),
            contentDescription = "Vehicle Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp) // Or whatever height you desire
        )
        IconToggleButton(
            checked = isFavorite.value,
            onCheckedChange = { isChecked ->
                coroutineScope.launch {
                    val response = repository.addToFavorites(context, vehicle.vehicleID)

                    if (response.isSuccess) {
                        Log.d("VehicleImageWithFavorite", "Favorites updated successfully.")
                        viewModel.toggleFavorite(vehicle.vehicleID, isChecked)
                        isFavorite.value = isChecked
                    } else {
                        Log.e("VehicleImageWithFavorite", "Error updating favorites.")
                    }
                }
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.nav_tab_favorite_icon),
                contentDescription = "Favorite",
                tint = if (isFavorite.value) Color(0xFFFFA500) else Color.White
            )
        }
    }
}

@Composable
fun VehicleDetailDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    viewModel: MapViewModel,
) {
    if (vehicle != null) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val imageWidth = screenWidth * 0.9f

        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                VehicleCard(
                    vehicle = vehicle, viewModel = viewModel, modifier = Modifier
                        .fillMaxWidth()
//            .clip(RoundedCornerShape(13.dp))
//            .background(Color.Black) // This sets the background color to black
//            .shadow(8.dp, RoundedCornerShape(10.dp)), // Apply shadow with the same rounded corners
                )
            },
            confirmButton = {}
        )
    }
}

@Composable
fun VehicleCard(vehicle: Vehicle, viewModel: MapViewModel, modifier: Modifier) {
    Column(
        modifier = modifier
    ) {

        VehicleImageWithFavorite(
            vehicle = vehicle,
            viewModel = viewModel
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
//                    VehicleInfo
                    Log.d("VehicleCard", "Clickable: ${vehicle.vehicleID}")

                }
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = vehicle.name)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${vehicle.rating}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * 0.7
                    )
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating Star",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(Modifier.weight(1f))
                Text("${vehicle.price}€")
            }
        }
        // end of this
    }
}

fun addMarkersToMap(
    context: Context,
    mapView: MapView,
    vehicles: List<Vehicle>,
    onVehicleClick: (Vehicle) -> Unit
) {
    mapView.overlays.clear()  // Clear existing overlays first

    vehicles.forEach { vehicle ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(vehicle.location.latitude, vehicle.location.longitude)
            title = vehicle.name
            snippet = "${vehicle.price}€"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = getMarkerIconWithText(
                context = context,
                vehicleTypeID = vehicle.vehicleTypeID,
                price = vehicle.price
            )

            setOnMarkerClickListener { marker, mapView ->
                onVehicleClick(vehicle)
                true  // true to indicate that the event has been handled
            }
        }
        mapView.overlays.add(marker)
    }
    mapView.invalidate()  // Refresh the map
}

fun getMarkerIconWithText(context: Context, vehicleTypeID: Int, price: Double): Drawable {
    val drawable = when (vehicleTypeID) {
        1 -> ContextCompat.getDrawable(context, R.drawable.circle_black) //cars
        2 -> ContextCompat.getDrawable(context, R.drawable.circle_red) //moto
        3 -> ContextCompat.getDrawable(context, R.drawable.circle_blue) //truck
        else ->
            ContextCompat.getDrawable(context, R.drawable.circle_black) // Default case
    }

    drawable?.let {
        // bitmap from the drawable
        val width = if (it.intrinsicWidth > 0) it.intrinsicWidth else 100
        val height = if (it.intrinsicHeight > 0) it.intrinsicHeight else 100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        it.setBounds(0, 0, canvas.width, canvas.height)
        it.draw(canvas)

        // Draw text
        val text = "$${price}"
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
        }
        // Calculate vertical position to center text
        val textHeight = paint.descent() - paint.ascent()
        val textOffset = (textHeight / 2) - paint.descent()
        canvas.drawText(text, canvas.width / 2f, canvas.height / 2f + textOffset, paint)

        return BitmapDrawable(context.resources, bitmap)
    }
    return ContextCompat.getDrawable(context, R.drawable.circle_red)!! // Fallback
}


fun initializeMap(context: Context): MapView {
    Configuration.getInstance()
        .load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val mapView = MapView(context)
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setBuiltInZoomControls(true)
//    mapView.isMultiTouchControls = true
    mapView.controller.setZoom(12.0)
    mapView.controller.setCenter(
        GeoPoint(
            44.81722374773659,
            20.460807455759323
        )
    )  //Belgrade starting location

    return mapView
}


