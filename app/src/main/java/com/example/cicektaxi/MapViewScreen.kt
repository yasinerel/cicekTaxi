package com.example.cicektaxi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapViewScreen(
    navController: NavController,
    userLatitude: Double = 41.261938,
    userLongitude: Double = 40.225562,
    distanceToBaseX: Double = 0.0
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display distance to Base X
        Text(
            text = "Çiçek Taksi Durağına Olan Uzaklık: %.2f km".format(distanceToBaseX),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Navigate to Profile button
        Button(
            onClick = { navController.navigate("profile") },
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(text = "Sürücü Profili")
        }

        // Map view
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)

                    // Center map on user location
                    controller.setZoom(15)
                    controller.setCenter(GeoPoint(userLatitude, userLongitude))

                    // Add user location marker
                    val marker = Marker(this)
                    marker.position = GeoPoint(userLatitude, userLongitude)
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "You are here"
                    overlays.add(marker)

                    // Base X marker (using an image)
                    val baseXMarker = Marker(this)
                    baseXMarker.position = GeoPoint(40.26189847142753, 40.22554413052036)  // Base X coordinates
                    baseXMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    baseXMarker.icon = context.resources.getDrawable(R.drawable.bb)  // aa.png image for Base X
                    baseXMarker.title = "ÇİÇEK TAKSİ DURAĞI"
                    overlays.add(baseXMarker)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Ensure the map takes up the remaining space
        )
    }
}
