package com.example.cicektaxi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
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

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private val BASE_X_LOCATION = GeoPoint(40.26195578140466, 40.225586375458356) // Replace with Base X coordinates
    }

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

    private fun calculateDistance(
        currentLatitude: Double,
        currentLongitude: Double,
        baseLatitude: Double,
        baseLongitude: Double
    ): Double {
        val earthRadius = 6371.0 // Earth's radius in kilometers

        val dLat = Math.toRadians(baseLatitude - currentLatitude)
        val dLon = Math.toRadians(baseLongitude - currentLongitude)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(currentLatitude)) * Math.cos(Math.toRadians(baseLatitude)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c // Distance in kilometers
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
                    val distance = calculateDistance(
                        latitude, longitude,
                        BASE_X_LOCATION.latitude, BASE_X_LOCATION.longitude
                    )
                    Log.d("DistanceToBaseX", "Distance: $distance km")
                    updateMapWithCurrentLocation(latitude, longitude, distance)
                }
            }
    }

    private fun updateMapWithCurrentLocation(latitude: Double, longitude: Double, distance: Double) {
        setContent {
            CicekTaxiTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "map") {
                    composable("map") {
                        MapViewScreen(
                            navController = navController,
                            userLatitude = latitude,
                            userLongitude = longitude,
                            distanceToBaseX = distance
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
}
