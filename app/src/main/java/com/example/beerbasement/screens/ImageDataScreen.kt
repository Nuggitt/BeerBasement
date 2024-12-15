package com.example.beerbasement.screens

import android.net.Uri
import android.graphics.Bitmap
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.beerbasement.model.TensorFlowModel
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDataScreen(imageUri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tensorFlowModel = remember { TensorFlowModel() }
    val prediction = remember { mutableStateOf("Loading...") }
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }

    val uri = Uri.parse(imageUri)
    val painter: Painter = rememberAsyncImagePainter(model = uri)

    // Load and preprocess image when the URI is passed
    androidx.compose.runtime.LaunchedEffect(imageUri) {
        if (imageUri != null) {
            try {
                val bitmap = preprocessImage(uri, context)
                imageBitmap.value = bitmap

                // Predict beer style using TensorFlow
                val result = tensorFlowModel.predictBeerStyle(bitmap)
                prediction.value = result
            } catch (e: IOException) {
                prediction.value = "Error loading image"
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Image Data") },
                colors = TopAppBarDefaults.topAppBarColors()
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            // Display image
            imageBitmap.value?.let {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                )
            }

            // Display prediction result
            Text(
                text = "Predicted Beer Style: ${prediction.value}",
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

fun preprocessImage(imageUri: Uri, context: android.content.Context): Bitmap {
    val contentResolver = context.contentResolver
    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)

    // Resize the image to the expected size for the model (e.g., 224x224)
    return Bitmap.createScaledBitmap(bitmap, 224, 224, true)
}
