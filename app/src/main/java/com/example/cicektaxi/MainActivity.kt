package com.example.cicektaxi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cicektaxi.ui.theme.CicekTaxiTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(
            applicationContext,
            android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check location permission
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permissions granted, get the location
            getCurrentLocation()
        } else {
            // Request permissions
            requestPermissionsLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        setContent {
            CicekTaxiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapViewComponent(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val latitude = it.latitude
                    val longitude = it.longitude
                    // Pass the current location to the MapViewComponent
                    updateMapWithCurrentLocation(latitude, longitude)
                }
            }
    }

    private fun updateMapWithCurrentLocation(latitude: Double, longitude: Double) {
        // You can use this function to update your MapView with the current location
        setContent {
            CicekTaxiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MapViewComponent(
                        modifier = Modifier.padding(innerPadding),
                        userLatitude = latitude,
                        userLongitude = longitude
                    )
                }
            }
        }
    }

    // Request permissions launcher
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                getCurrentLocation()
            } else {
                Log.e("LocationPermission", "Permission denied")
            }
        }
}


@Composable
fun MapViewComponent(
    modifier: Modifier = Modifier,
    userLatitude: Double = 41.261938, // default, will be updated dynamically
    userLongitude: Double = 40.225562  // default, will be updated dynamically
) {
    AndroidView(
        factory = { context ->
            MapView(context).apply {
                setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                // Set initial map view and zoom
                controller.setZoom(15)
                controller.setCenter(GeoPoint(userLatitude, userLongitude))

                // Add a Marker
                val marker = Marker(this)
                marker.position = GeoPoint(userLatitude, userLongitude)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "You are here"
                overlays.add(marker)
            }
        },
        modifier = modifier.fillMaxSize()
    )
}
