package com.example.beerbasement.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageLabelingScreen(savedUri: Uri) {
    val context = LocalContext.current
    var labels by remember { mutableStateOf(listOf<String>()) }

    // Load the bitmap from the URI
    val bitmap = remember(savedUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(savedUri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e("ImageLabeling", "Error loading bitmap: ${e.message}")
            null
        }
    }

    // Process the image with ML Kit
    bitmap?.let {
        val inputImage = InputImage.fromBitmap(it, 0)
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        LaunchedEffect(inputImage) {
            labeler.process(inputImage)
                .addOnSuccessListener { imageLabels ->
                    labels = imageLabels.map { it.text }
                }
                .addOnFailureListener { e ->
                    Log.e("ImageLabeling", "Labeling failed: ${e.message}")
                }
        }
    } ?: run {
        Log.e("ImageLabeling", "Bitmap is null")
    }

    // Scaffold Layout
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Image Labeling")
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
                    text = "Detected Labels",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground // Adjusts to both light/dark
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // If no labels, show a loading message or hint
                if (labels.isEmpty()) {
                    Text(
                        text = "Detecting objects in the image...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.Gray
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    // List of detected labels
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(labels) { label ->
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface // Always visible text color
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)) // Light background for items
                                    .padding(8.dp)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Retry button if image loading or processing fails
                if (bitmap == null || labels.isEmpty()) {
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


