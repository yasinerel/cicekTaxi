package com.example.cicektaxi

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.cicektaxi.ui.theme.CicekTaxiTheme

@Composable
fun ProfileScreen() {
    // Mock data for now
    val userName = "John Doe"
    val userEmail = "johndoe@example.com"

    // Profile Screen Layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Profile Image Placeholder
        Image(
            painter = painterResource(id = R.drawable.profile_icon), // Replace with actual user profile image later
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        // Display User Name and Email
        Text(
            text = "Name: $userName",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Email: $userEmail",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ride History Placeholder
        Text(
            text = "Ride History",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Coming Soon...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CicekTaxiTheme {
        ProfileScreen()
    }
}
