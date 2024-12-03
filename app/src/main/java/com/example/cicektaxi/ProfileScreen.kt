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
    val userName = "DAŞTAN 1"
    val userEmail = "dastan@dastan.com"

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
            painter = painterResource(id = R.drawable.aa), // Replace with actual user profile image later
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .padding(bottom = 16.dp)
        )

        // Display User Name and Email
        Text(
            text = "İSİM: $userName",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Email: $userEmail",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Ride History Placeholder
        Text(
            text = "Sürüş Geçmişi",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Bugün -şu- Rotalarda -bu- Kadar KM Sürdünüz...",
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
