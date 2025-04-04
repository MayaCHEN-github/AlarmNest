package edu.cuhk.csci3310.csci3310project.location

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import edu.cuhk.csci3310.csci3310project.location.googlemap.MapCard
import edu.cuhk.csci3310.csci3310project.location.googlemap.PlaceAutocompleteWidget

@Composable
fun LocationSetScreen() {
    var currentLocation by remember { mutableStateOf(LatLng(22.4194, 114.2068)) }
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MapCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                location = currentLocation
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PlaceAutocompleteWidget(
                onPlaceSelected = { newLocation ->
                    currentLocation = newLocation
                }
            )
        }
    }
} 