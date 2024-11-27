package com.example.beerbasement.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TextScannerScreen(
    recognizedText: String?, // This is now just a string
    navController: NavController // Add the navController parameter
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title text
        Text(
            text = "Recognized Text:",
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display the recognized text or a fallback message
        val displayText = recognizedText?.takeIf { it.isNotEmpty() } ?: "No text recognized"
        Text(
            text = displayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Spacer for visual separation
        Spacer(modifier = Modifier.height(16.dp))

        // Go Back button
        Button(onClick = { navController.popBackStack() }) {
            Text("Go Back")
        }
    }
}
