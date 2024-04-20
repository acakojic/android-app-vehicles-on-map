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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.wear.compose.material.dialog.Dialog
import coil.compose.rememberImagePainter
import com.acakojic.zadataktcom.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory


class MapViewModel(private val customRepository: CustomRepository, private val context: Context) :
    ViewModel() {
    private val _vehicles = MutableLiveData<List<Vehicle>>()
    val vehicles: LiveData<List<Vehicle>> = _vehicles

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
}

@Composable
fun VehicleMapScreen(viewModel: MapViewModel, onVehicleClick: (Vehicle) -> Unit) {
    val context = LocalContext.current
    val vehicles = viewModel.vehicles.observeAsState(listOf()).value
    val mapView = remember { initializeMap(context) }

    // Use Side Effects to update markers when vehicles data changes
    LaunchedEffect(vehicles) {
        mapView.overlays.clear()
        addMarkersToMap(
            context = context,
            mapView = mapView,
            vehicles = vehicles,
            onVehicleClick = onVehicleClick
        )
        mapView.invalidate()
    }

    AndroidView(factory = { mapView })
}

@Composable
fun VehicleImageWithFavorite(vehicle: Vehicle, onFavoriteToggle: (Vehicle) -> Unit) {
    Box(contentAlignment = Alignment.TopEnd) {
        Image(
            painter = rememberImagePainter(vehicle.imageURL),
            contentDescription = "Vehicle Image",
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )
        IconToggleButton(
            checked = vehicle.isFavorite,
            onCheckedChange = {
                onFavoriteToggle(vehicle)
            }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.nav_tab_favorite_icon),
                contentDescription = "Favorite",
                tint = (if (vehicle.isFavorite) Color(0xFFFFA500) else Color.White) // orange if favorite, white if not
            )
        }
    }
}

@Composable
fun VehicleDetailDialog(
    vehicle: Vehicle?,
    onDismiss: () -> Unit,
    onFavoriteToggle: (Vehicle) -> Unit
) {
    if (vehicle != null) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val imageWidth = screenWidth * 0.9f

        AlertDialog(
            onDismissRequest = onDismiss,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
                ) {

                    VehicleImageWithFavorite(vehicle = vehicle, onFavoriteToggle = onFavoriteToggle)

//                        Image(
//                            painter = rememberImagePainter(vehicle.imageURL),
//                            contentDescription = "Vozilo slika",
//                        modifier = Modifier
////                            .size(imageWidth) //set fixed size
//                            .clip(RoundedCornerShape(8.dp))
//                        )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = vehicle.name)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${vehicle.rating}")
                        Spacer(Modifier.weight(1f))
                        Text("${vehicle.price}€")
                    }


                }
            },
            confirmButton = {}
        )
//        Column {
//
//            VehicleImageWithFavorite(vehicle = vehicle, onFavoriteToggle = onFavoriteToggle)
//        }
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
            snippet = "Price: ${vehicle.price}€"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = getMarkerIconWithText(
                context = context,
                vehicleTypeID = vehicle.vehicleTypeID,
                price = vehicle.price
            )

            setOnMarkerClickListener { marker, mapView ->
                onVehicleClick(vehicle)
                true  // Return true to indicate that the event has been handled
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
        // Create a bitmap from the drawable
        val width = if (it.intrinsicWidth > 0) it.intrinsicWidth else 100
        val height = if (it.intrinsicHeight > 0) it.intrinsicHeight else 100

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        it.setBounds(0, 0, canvas.width, canvas.height)
        it.draw(canvas)

        // Draw text on the bitmap
        val text = "$${price}"
        val paint = Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 30f // Adjust size as needed
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


