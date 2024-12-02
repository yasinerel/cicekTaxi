package com.example.cicektaxi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.cicektaxi.ui.theme.CicekTaxiTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(
            applicationContext,
            android.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1011011668697-5a79kojghi6e89fs5nm63bnenmbcs1ma.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize FusedLocationProviderClient
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
                val navController = rememberNavController()

                // Set up the navigation
                NavHost(navController = navController, startDestination = "map") {
                    composable("map") {
                        MapViewScreen(navController = navController)
                    }
                    composable("profile") {
                        ProfileScreen()
                    }
                }
            }
        }

        signIn()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(Exception::class.java)
                account?.let {
                    firebaseAuthWithGoogle(it)
                }
            } catch (e: Exception) {
                Log.e("SignInError", "Google Sign-In failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("FirebaseAuth", "User signed in: ${user?.displayName}")
                } else {
                    Log.e("FirebaseAuth", "Sign-in failed", task.exception)
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
        setContent {
            CicekTaxiTheme {
                val navController = rememberNavController()

                // Update the navigation with the new location
                NavHost(navController = navController, startDestination = "map") {
                    composable("map") {
                        MapViewScreen(
                            navController = navController,
                            userLatitude = latitude,
                            userLongitude = longitude
                        )
                    }
                    composable("profile") {
                        ProfileScreen()
                    }
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

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapViewScreen(
    navController: NavController,
    userLatitude: Double = 41.261938, // Default values
    userLongitude: Double = 40.225562 // Default values
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Çiçek Taxi") },
                actions = {
                    IconButton(onClick = {
                        // Navigate to profile screen
                        navController.navigate("profile")
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.profile_icon), // Use your profile icon
                            contentDescription = "Profile Icon"
                        )
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Add the map view
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
                    modifier = Modifier.fillMaxSize()
                )

                // Add a button to navigate to the Profile screen
                Button(
                    onClick = {
                        navController.navigate("profile")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(text = "Go to Profile")
                }
            }
        }
    )
}
