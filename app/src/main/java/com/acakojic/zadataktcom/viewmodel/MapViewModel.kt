package com.acakojic.zadataktcom.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
fun VehicleMapScreen(viewModel: MapViewModel) {
    val context = LocalContext.current
    val vehicles = viewModel.vehicles.observeAsState(listOf()).value
    val mapView = remember { initializeMap(context) }

    // Use Side Effects to update markers when vehicles data changes
    LaunchedEffect(vehicles) {
        mapView.overlays.clear()
        addMarkersToMap(context = context, mapView = mapView, vehicles = vehicles)
        mapView.invalidate()
    }

    AndroidView(factory = { mapView })
}

fun addMarkersToMap(context: Context, mapView: MapView, vehicles: List<Vehicle>) {
    vehicles.forEach { vehicle ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(vehicle.location.latitude, vehicle.location.longitude)
            title = vehicle.name
            snippet = "Price: ${vehicle.price}€"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            icon = getMarkerIconWithText(context = context, vehicleTypeID = vehicle.vehicleTypeID, price = vehicle.price)
        }
        mapView.overlays.add(marker)
    }
}

fun getMarkerIconWithText(context: Context, vehicleTypeID: Int, price: Double): Drawable {
    val drawable = when (vehicleTypeID) {
        1 -> ContextCompat.getDrawable(context, R.drawable.circle_black)
        2 -> ContextCompat.getDrawable(context, R.drawable.circle_red)
        3 -> ContextCompat.getDrawable(context, R.drawable.circle_blue)
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
            color = Color.WHITE
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
    Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))

    val mapView = MapView(context)
    mapView.setTileSource(TileSourceFactory.MAPNIK)
    mapView.setBuiltInZoomControls(true)
//    mapView.isMultiTouchControls = true
    mapView.controller.setZoom(12.0)
    mapView.controller.setCenter(GeoPoint(44.81722374773659, 20.460807455759323))  //Belgrade starting location

    return mapView
}


