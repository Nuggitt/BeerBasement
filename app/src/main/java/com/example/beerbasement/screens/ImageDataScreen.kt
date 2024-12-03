import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.*
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Collections

@Composable
fun ImageDataScreen(imageUri: Uri) {
    val context = LocalContext.current
    var recognizedText by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Process the image when the URI changes
    LaunchedEffect(imageUri) {
        try {
            // Retrieve the bitmap from the Uri using openInputStream
            val inputStream = context.contentResolver.openInputStream(imageUri)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Send the bitmap to Google Cloud Vision API
            bitmap?.let {
                callCloudVision(it, context) { recognizedText = it }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error processing image: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // UI Layout
    Column(modifier = Modifier.padding(16.dp)) {
        // Display the image if available
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = "Captured Image", modifier = Modifier.fillMaxWidth())
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the recognized text
        Text(
            text = if (recognizedText.isNotEmpty()) "Recognized Text: $recognizedText" else "No text recognized",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

fun getVisionCredentialsFromAssets(context: Context): GoogleCredentials {
    val assetManager = context.assets
    val inputStream = assetManager.open("beerbasementproject-d7f15e4ed609.json") // Your service account JSON file
    return GoogleCredentials.fromStream(inputStream).createScoped(
        listOf("https://www.googleapis.com/auth/cloud-platform")
    )
}

suspend fun callCloudVision(bitmap: Bitmap, context: Context, onTextRecognized: (String) -> Unit) {
    try {
        // Perform image processing on a background thread to prevent blocking the UI
        withContext(Dispatchers.IO) {
            // Step 1: Get credentials from assets (service account key file)
            val credentials = getVisionCredentialsFromAssets(context)

            // Step 2: Set up the gRPC transport channel
            val channel = ManagedChannelBuilder.forAddress("vision.googleapis.com", 443)
                .useTransportSecurity() // Enable secure communication
                .build()

            val transportChannel = GrpcTransportChannel.create(channel)

            // Step 3: Set up ImageAnnotatorSettings with credentials and channel
            val imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider { credentials }
                .setTransportChannelProvider(FixedTransportChannelProvider.create(transportChannel))
                .build()

            // Step 4: Initialize the ImageAnnotatorClient
            val imageAnnotatorClient = ImageAnnotatorClient.create(imageAnnotatorSettings)

            // Step 5: Prepare the image and request
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val image = Image.newBuilder()
                .setContent(ByteString.copyFrom(stream.toByteArray()))
                .build()

            val feature = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .setMaxResults(5)
                .build()

            val request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build()

            // Step 6: Make the Vision API call
            val response = imageAnnotatorClient.batchAnnotateImages(listOf(request))

            // Step 7: Handle the API response on the main thread
            withContext(Dispatchers.Main) {
                handleVisionResponse(response, context, onTextRecognized)
            }

            // Step 8: Close the channel to free resources
            imageAnnotatorClient.close()
            channel.shutdownNow()
        }
    } catch (e: Exception) {
        Log.e("ImageProcessingError", "Error processing image", e)
        Toast.makeText(context, "Error processing image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}


fun handleVisionResponse(response: BatchAnnotateImagesResponse, context: Context, onTextRecognized: (String) -> Unit) {
    try {
        // Get the text annotations from the response (we're expecting a list of TextAnnotations)
        val textAnnotations = response.responsesList[0].textAnnotationsList
        val recognizedText = StringBuilder()

        // Iterate through the text annotations and append the recognized text
        for (annotation in textAnnotations) {
            recognizedText.append(annotation.description).append("\n") // 'description' is the actual text detected
        }

        // Pass the recognized text to the UI
        onTextRecognized(recognizedText.toString())

    } catch (e: Exception) {
        val toast = Toast.makeText(context, "Error processing API response: ${e.localizedMessage}", Toast.LENGTH_LONG)
        toast.show()

        // Custom duration (e.g., 5 seconds)
        Handler(Looper.getMainLooper()).postDelayed({ toast.cancel() }, 5000000)
    }
}

fun encodeBitmapToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val imageBytes = baos.toByteArray()
    return Base64.encodeToString(imageBytes, Base64.DEFAULT)
}
