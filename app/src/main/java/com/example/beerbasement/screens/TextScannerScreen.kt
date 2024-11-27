package com.example.beerbasement.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextScannerScreen(savedUri: Uri) {
    val context = LocalContext.current
    var detectedText by remember { mutableStateOf("") }

    // Load the bitmap from the URI
    val bitmap = remember(savedUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(savedUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("TextRecognition", "Error loading bitmap: ${e.message}")
            null
        }
    }

    // Process the image with ML Kit's Text Recognition
    bitmap?.let {
        val inputImage = InputImage.fromBitmap(it, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        LaunchedEffect(inputImage) {
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    // Process the recognized text
                    detectedText = visionText.text
                }
                .addOnFailureListener { e ->
                    Log.e("TextRecognition", "Text recognition failed: ${e.message}")
                }
        }
    } ?: run {
        Log.e("TextRecognition", "Bitmap is null")
    }

    // Scaffold Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Text Recognition")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.background) // Background adapts to theme
            ) {
                // Title Section
                Text(
                    text = "Detected Text",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground // Adjusts to both light/dark
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // If no text is detected, show a loading message or hint
                if (detectedText.isEmpty()) {
                    Text(
                        text = "Detecting text in the image...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // Display the detected text
                    Text(
                        text = detectedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface // Always visible text color
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)) // Light background for text
                            .padding(8.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Retry button if image loading or processing fails
                if (bitmap == null || detectedText.isEmpty()) {
                    Button(
                        onClick = {
                            // Retry logic could be added here, for example, reloading the image
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
    )
}
