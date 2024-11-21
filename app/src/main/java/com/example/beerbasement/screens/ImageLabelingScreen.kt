package com.example.beerbasement.screens

import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.beerbasement.model.LabelingViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

@Composable
fun ImageLabelingScreen(savedUri: Uri) {
    val context = LocalContext.current

    // Access the ViewModel
    val labelingViewModel = viewModel<LabelingViewModel>()

    // Try to load the bitmap from the URI
    val bitmap = try {
        MediaStore.Images.Media.getBitmap(context.contentResolver, savedUri)
    } catch (e: Exception) {
        Log.e("ImageLabeling", "Error converting image URI to Bitmap: ${e.message}")
        null
    }

    // Process the image and update the ViewModel's labels
    bitmap?.let {
        val inputImage = InputImage.fromBitmap(it, 0)
        val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        imageLabeler.process(inputImage)
            .addOnSuccessListener { labels ->
                val labelList = labels.map { it.text }
                // Update the ViewModel's labels
                labelingViewModel.labels = labelList
            }
            .addOnFailureListener { e ->
                Log.e("ImageLabeling", "Failed to label image: ${e.message}")
            }
    } ?: run {
        Log.e("ImageLabeling", "Bitmap is null, cannot process image.")
    }

    // Display the labels from the ViewModel
    DisplayLabels(labelingViewModel.labels)
}

@Composable
fun DisplayLabels(labelList: List<String>) {
    Column {
        Text("Detected Labels:")
        labelList.forEach { label ->
            Text(label)
        }
    }
}
