package edu.cuhk.csci3310.csci3310project.location.googlemap

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun MapCard(
    modifier: Modifier = Modifier.fillMaxWidth().height(230.dp),
    location: LatLng = LatLng(22.4194, 114.2068),
    zoomLevel: Float = 15f
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, zoomLevel)
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        )
    }
}

@Composable
fun GoogleMapScreen(
    location: LatLng = LatLng(22.4194, 114.2068), // 默认位置为香港中文大学
    zoomLevel: Float = 15f, // 默认缩放级别
    locationName: String = "香港中文大学" // 默认地点名称
) {
    // 设置相机位置状态
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(location, zoomLevel)
    }
    
    // 创建Google地图
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    )
}

