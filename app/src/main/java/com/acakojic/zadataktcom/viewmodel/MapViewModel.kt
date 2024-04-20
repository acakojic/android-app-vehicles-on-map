package com.acakojic.zadataktcom.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import com.acakojic.zadataktcom.service.CustomRepository
import com.acakojic.zadataktcom.service.Vehicle

import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.osmdroid.config.Configuration.getInstance
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import java.io.File



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

    AndroidView(factory = { ctx ->
        initializeMap(ctx).also { mapView ->
            addMarkersToMap(mapView, vehicles)
        }
    })
}


fun initializeMap(context: Context): MapView {
    getInstance().load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
    getInstance().osmdroidBasePath = File(context.cacheDir, "osmdroid").apply { mkdirs() }
    getInstance().osmdroidTileCache = File(getInstance().osmdroidBasePath, "tile").apply { mkdirs() }

    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setBuiltInZoomControls(true)
//        isMultiTouchControls = true
        controller.setZoom(11.5)
        controller.setCenter(GeoPoint(44.81722374773659, 20.460807455759323))  //Belgrade starting location
    }
}


fun addMarkersToMap(mapView: MapView, vehicles: List<Vehicle>) {
    vehicles.forEach { vehicle ->
        val marker = Marker(mapView).apply {
            position = GeoPoint(vehicle.location.latitude, vehicle.location.longitude)
            title = vehicle.name
            snippet = "Price: ${vehicle.price}€"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
        mapView.overlays.add(marker)
    }
    mapView.invalidate()
}

//@Composable
//fun GoogleMapComponent(mapViewModel: MapViewModel) {
//    val singapore = LatLng(1.35, 103.87)
//    val cameraPositionState: CameraPositionState = rememberCameraPositionState {
//        position = CameraPosition.fromLatLngZoom(singapore, 11f) // Default zoom level
//    }
//    val mapProperties by remember {
//        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
//    }
//    val uiSettings by remember {
//        mutableStateOf(MapUiSettings(zoomControlsEnabled = false))
//    }
//
//    Box(modifier = Modifier.fillMaxSize()) {
//        GoogleMap(
//            modifier = Modifier.matchParentSize(),
//            properties = mapProperties,
//            uiSettings = uiSettings,
//            cameraPositionState = cameraPositionState
//        ) {
//            // Add markers here
//            mapViewModel.vehicles.value.forEach { vehicle ->
//                Marker(
//                    state = MarkerState(
//                        position = LatLng(
//                            vehicle.location.latitude,
//                            vehicle.location.longitude
//                        )
//                    ),
//                    title = vehicle.name,
//                    snippet = "Price: ${vehicle.price}€"
//                )
//            }
//        }
//    }
//}

