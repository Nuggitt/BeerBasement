package com.example.beerbasement.screens

import android.net.Uri
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.provider.MediaStore
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

data class BeerPrediction(
    val name: String,
    val style: String,
    val abv: String,
    val volume: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDataScreen(imageUri: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tensorFlowModel = remember { TensorFlowModel() }
    val beerPrediction = remember { mutableStateOf<BeerPrediction?>(null) }
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
        Log.d("ImageDataScreen", "Received imageUri: $imageUri")
        if (imageUri != null) {
            try {
                val bitmap = preprocessImage(uri, context)
                Log.d("ImageDataScreen", "Bitmap preprocessed: ${bitmap.width}x${bitmap.height}")
                imageBitmap.value = bitmap

                // TensorFlow prediction for beer details
                val byteBuffer = tensorFlowModel.convertBitmapToByteBuffer(bitmap)
                val result = tensorFlowModel.predictBeerDetails(byteBuffer)
                Log.d("ImageDataScreen", "Prediction result: $result")

                // Update UI with prediction result
                beerPrediction.value = BeerPrediction(
                    name = result["name"] as? String ?: "Unknown",  // Safely cast or use default "Unknown"
                    style = result["style"] as? String ?: "Unknown",
                    abv = result["abv"]?.toString() ?: "Unknown",  // Use .toString() for non-String values
                    volume = result["volume"]?.toString() ?: "Unknown"
                )
            } catch (e: IOException) {
                Log.e("ImageDataScreen", "Error loading image: ${e.message}")
                beerPrediction.value = null
            } catch (e: Exception) {
                Log.e("ImageDataScreen", "Error during prediction: ${e.message}")
                beerPrediction.value = null
            }
        } else {
            Log.e("ImageDataScreen", "No imageUri provided")
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()) // Makes the Column scrollable
        ) {
            // Display image
            imageBitmap.value?.let {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth() // Ensure image doesn't take too much space
                        .height(200.dp) // Set a fixed height for the image
                )
            }

            // Display prediction result
            beerPrediction.value?.let { prediction ->
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Name: ${prediction.name}")
                    Text("Style: ${prediction.style}")
                    Text("ABV: ${prediction.abv}")
                    Text("Volume: ${prediction.volume}")
                }
            } ?: run {
                Text(
                    text = "No prediction available",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

fun preprocessImage(imageUri: Uri, context: android.content.Context): Bitmap {
    val contentResolver = context.contentResolver
    val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
    }

    // Ensure the bitmap is mutable and uses ARGB_8888 configuration
    val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

    // Resize the bitmap to the required dimensions (224x224)
    val resizedBitmap = Bitmap.createScaledBitmap(mutableBitmap, 224, 224, true)

    return resizedBitmap
}
