package com.example.beerbasement.screens

import android.net.Uri
import android.graphics.Bitmap
import android.graphics.ImageDecoder
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

    // Load the TensorFlow Lite model and preprocess the image
    LaunchedEffect(Unit) {
        tensorFlowModel.loadModel(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            tensorFlowModel.close()
        }
    }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            try {
                val bitmap = preprocessImage(uri, context)
                imageBitmap.value = bitmap

                // Predict beer style using TensorFlow
                val result = tensorFlowModel.predictBeerStyle(bitmap)
                prediction.value = result
            } catch (e: IOException) {
                prediction.value = "Error loading image"
            } catch (e: Exception) {
                prediction.value = "Error during prediction"
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
    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    } else {
        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

    // Normalize the image for TensorFlow if required
    return resizedBitmap
}
