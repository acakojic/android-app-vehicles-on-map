package com.acakojic.zadataktcom.viewmodel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.acakojic.zadataktcom.service.Vehicle

class AllVehiclesViewModel {
}

@Composable
fun ShowVehiclesScreen(viewModel: MapViewModel, navController: NavHostController) {
    var context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    val selectedTab by viewModel.selectedVehicleType

    val allVehicles = viewModel.vehicles.observeAsState(listOf()).value ?: listOf()
    var sortedAndFilteredVehicles by remember { mutableStateOf(allVehicles) }

    var selectedSortOrder by remember { mutableStateOf(SortOrder.PriceAsc) }

    val mapView = remember { initializeMap(context) }

    LaunchedEffect(searchQuery, selectedSortOrder) {
        sortedAndFilteredVehicles = allVehicles
            .filter { it.name.contains(searchQuery, ignoreCase = true) }
            .let { list ->
                when (selectedSortOrder) {
                    SortOrder.PriceAsc -> list.sortedBy { it.price }
                    SortOrder.PriceDesc -> list.sortedByDescending { it.price }
                }
            }
    }

    LaunchedEffect(viewModel.vehicles) {
        mapView.overlays.clear()
        viewModel.vehicles.value?.let { vehicles ->
            viewModel.updateMapMarkers(mapView, vehicles) { vehicle ->
//            selectedVehicle = vehicle
//            showDialog = true)
            }
        }
    }

    Column {

        SearchAndSortRow(
            searchText = searchQuery,
            onSearchQueryChanged = { query -> searchQuery = query },
            selectedSortOrder = selectedSortOrder,
            onSortOrderSelected = { sortOrder -> selectedSortOrder = sortOrder }
        )

        TabRow(selectedTabIndex = selectedTab.ordinal) {
            VehicleType.entries.forEachIndexed { index, type ->
                Tab(
                    selected = selectedTab.ordinal == index,
                    onClick = { viewModel.setVehicleType(type) },
                    text = { Text(type.name) }
                )
            }
        }

        LazyColumn {
            items(sortedAndFilteredVehicles) { vehicle ->
                Spacer(modifier = Modifier.height(20.dp))
                VehicleCard(
                    vehicle = vehicle,
                    viewModel = viewModel,
                    modifier = getModifierForFavorite(),
                    navController = navController
                )
            }
        }

        // Ensure that vehicle updates are reflected in the UI
//        val allVehicles by viewModel.vehicles.observeAsState(emptyList())
//        val sortedAndFilteredVehicles = allVehicles?.filter { it.vehicleTypeID == selectedTab.typeId }

        // Include additional filtering/sorting if necessary

//        if (sortedAndFilteredVehicles != null) {
//
//            LazyColumn {
//                items(sortedAndFilteredVehicles) { vehicle ->
//                    Spacer(modifier = Modifier.height(20.dp))
//                    VehicleCard(
//                        vehicle = vehicle,
//                        viewModel = viewModel,
//                        modifier = getModifierForFavorite(),
//                        navController = navController
//                    )
//                }
//            }
//        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SearchAndSortRow(
//    searchText: String,
//    onSearchQueryChanged: (String) -> Unit,
//    selectedSortOrder: SortOrder,
//    onSortOrderSelected: (SortOrder) -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        TextField(
//            value = searchText,
//            onValueChange = onSearchQueryChanged,
//            modifier = Modifier
//                .weight(1f)
//                .padding(end = 8.dp), // This adds space after the TextField
//            placeholder = { Text("Pretraži vozila") },
//            singleLine = true,
//            colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent)
//        )
//
//        // Spacer with fixed width for space between TextField and Sort text
//        Spacer(modifier = Modifier.width(8.dp))
//
//        SortDropdownMenu(
//            selectedSortOrder = selectedSortOrder,
//            onSortOrderSelected = onSortOrderSelected
//        )
//    }
//}


//@Composable
//fun SortDropdownMenu(
//    selectedSortOrder: SortOrder,
//    onSortOrderSelected: (SortOrder) -> Unit
//) {
//    var expanded by remember { mutableStateOf(false) }
//
//    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
//        Text(
//            text = selectedSortOrder.label,
//            modifier = Modifier
//                .fillMaxWidth()
//                .clickable { expanded = true }
//                .padding(16.dp)
//        )
//        DropdownMenu(
//            expanded = expanded,
//            onDismissRequest = { expanded = false }
//        ) {
//            SortOrder.entries.forEach { sortOrder ->
//                DropdownMenuItem(
//                    onClick = {
//                        onSortOrderSelected(sortOrder)
//                        expanded = false
//                    },
//                    text = {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(text = sortOrder.label)
//                            if (selectedSortOrder == sortOrder) {
//                                Icon(
//                                    imageVector = Icons.Filled.Check,
//                                    contentDescription = "Selected",
//                                    tint = Color(0xFFFFA500) // Orange color for the check icon
//                                )
//                            }
//                        }
//                    }
//                )
//            }
//        }
//    }
//}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchAndSortRow(
    searchText: String,
    onSearchQueryChanged: (String) -> Unit,
    selectedSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            value = searchText,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Search vehicles") },
            singleLine = true,
            colors = textFieldColors(containerColor = Color.Transparent)
        )

        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
            Text(
                text = "Sort",
                color = Color(0xFFFFA500), // Use theme color here if possible
                modifier = Modifier
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                SortOrder.entries.forEach { sortOrder ->
                    DropdownMenuItem(
                        onClick = {
                            onSortOrderSelected(sortOrder)
                            expanded = false
                        }, text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = sortOrder.label)
                                Spacer(modifier = Modifier.width(8.dp))
                                if (selectedSortOrder == sortOrder) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFFFFA500) // Orange color for the check icon
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

enum class VehicleType(val typeId: Int) {
    Auto(1),
    Motor(2),
    Kamion(3)
}

enum class SortOrder(val label: String) {
    PriceAsc("Po ceni - Prvo jeftinije"),
    PriceDesc("Po ceni - Prvo skuplje")
}
