package com.example.beerbasement.screens

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

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

    // Display the labels
    Column {
        Text("Detected Labels:")
        labels.forEach { label ->
            Text(label)
        }
    }
}
