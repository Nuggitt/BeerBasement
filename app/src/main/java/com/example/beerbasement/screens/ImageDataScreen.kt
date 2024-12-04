import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.ImageAnnotatorSettings
import com.google.protobuf.ByteString
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDataScreen(imageUri: Uri, signOut: () -> Unit) {
    val context = LocalContext.current
    var recognizedLogos by remember { mutableStateOf("") }
    var recognizedText by remember { mutableStateOf("") }
    var recognizedLabels by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Process the image when the URI changes
    LaunchedEffect(imageUri) {
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            var originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Correct the orientation of the image
            originalBitmap = correctBitmapOrientation(context, imageUri, originalBitmap)

            // Reset messages
            recognizedLogos = ""
            recognizedText = ""
            recognizedLabels = ""
            errorMessage = ""

            originalBitmap?.let {
                bitmap = it // Set the corrected bitmap
                callCloudVision(it, context) { (logos, text, labels) ->
                    recognizedLogos = logos
                    recognizedText = text
                    recognizedLabels = labels
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error processing image: ${e.localizedMessage}"
        }
    }

    // Scaffold Layout
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Image Data") },
                actions = {
                    IconButton(onClick = { signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Display the image in portrait orientation
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Captured Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(275.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display success message for recognized logos
                if (recognizedLogos.isNotEmpty()) {
                    Text(
                        text = "Detected Logos:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = recognizedLogos,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else if (recognizedLogos.isEmpty() && recognizedText.isEmpty() && recognizedLabels.isEmpty()) {
                    Text(
                        text = "No logos detected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Display recognized text
                if (recognizedText.isNotEmpty()) {
                    Text(
                        text = "Detected Text:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = recognizedText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Display recognized labels
                if (recognizedLabels.isNotEmpty()) {
                    Text(
                        text = "Detected Labels:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = recognizedLabels,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Display error message, if any
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = "Error:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    )
}

/**
 * Corrects the orientation of a bitmap based on its Exif metadata.
 */
fun correctBitmapOrientation(context: Context, uri: Uri, bitmap: Bitmap?): Bitmap? {
    if (bitmap == null) return null

    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val exif = android.media.ExifInterface(inputStream!!)
    val orientation = exif.getAttributeInt(
        android.media.ExifInterface.TAG_ORIENTATION,
        android.media.ExifInterface.ORIENTATION_NORMAL
    )

    val matrix = android.graphics.Matrix()

    when (orientation) {
        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
    }

    inputStream.close()

    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun getVisionCredentialsFromAssets(context: Context): GoogleCredentials {
    val assetManager = context.assets
    val inputStream =
        assetManager.open("beerbasementproject-d7f15e4ed609.json") // Your service account JSON file
    return GoogleCredentials.fromStream(inputStream).createScoped(
        listOf("https://www.googleapis.com/auth/cloud-platform")
    )
}

suspend fun callCloudVision(
    bitmap: Bitmap,
    context: Context,
    onResult: (Triple<String, String, String>) -> Unit
) {
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

            // Step 6: Set up features for different detections (Logo, Text, and Labels)
            val logoFeature = Feature.newBuilder()
                .setType(Feature.Type.LOGO_DETECTION)
                .setMaxResults(5)
                .build()

            val textFeature = Feature.newBuilder()
                .setType(Feature.Type.TEXT_DETECTION)
                .setMaxResults(5)
                .build()

            val labelFeature = Feature.newBuilder()
                .setType(Feature.Type.LABEL_DETECTION)
                .setMaxResults(5)
                .build()

            val request = AnnotateImageRequest.newBuilder()
                .addFeatures(logoFeature)
                .addFeatures(textFeature)
                .addFeatures(labelFeature)
                .setImage(image)
                .build()

            // Step 7: Make the Vision API call
            val response = imageAnnotatorClient.batchAnnotateImages(listOf(request))

            // Step 8: Handle the API response on the main thread
            withContext(Dispatchers.Main) {
                handleVisionResponse(response, context, onResult)
            }

            // Step 9: Close the channel to free resources
            imageAnnotatorClient.close()
            channel.shutdownNow()
        }
    } catch (e: Exception) {
        Log.e("ImageProcessingError", "Error processing image", e)
        Toast.makeText(context, "Error processing image: ${e.localizedMessage}", Toast.LENGTH_SHORT)
            .show()
    }
}

fun handleVisionResponse(
    response: BatchAnnotateImagesResponse,
    context: Context,
    onResult: (Triple<String, String, String>) -> Unit
) {
    try {
        Log.d("VisionResponse", "Response: ${response.toString()}")

        // Get the logo annotations from the response
        val logoAnnotations = response.responsesList[0].logoAnnotationsList
        val textAnnotations = response.responsesList[0].textAnnotationsList
        val labelAnnotations = response.responsesList[0].labelAnnotationsList

        // Prepare StringBuilder to store recognized logos, text, and labels
        val recognizedLogos = StringBuilder()
        val recognizedText = StringBuilder()
        val recognizedLabels = StringBuilder()

        // Extract logo information
        for (annotation in logoAnnotations) {
            recognizedLogos.append("Logo: ${annotation.description}, Confidence: ${annotation.score}\n")
        }

        // Extract text information (if any)
        for (annotation in textAnnotations) {
            recognizedText.append("Detected Text: ${annotation.description}\n")
        }

        // Extract label information (if any)
        for (annotation in labelAnnotations) {
            recognizedLabels.append("Label: ${annotation.description}, Confidence: ${annotation.score}\n")
        }

        // Return the combined result to the UI
        onResult(
            Triple(
                recognizedLogos.toString(),
                recognizedText.toString(),
                recognizedLabels.toString()
            )
        )
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Error processing API response: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}