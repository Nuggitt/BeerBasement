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
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.vision.v1.*
import com.google.protobuf.ByteString
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
            Toast.makeText(context, "Error processing image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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
    val inputStream: InputStream = context.assets.open("beerbasementproject-4cd09c30fd67.json")
    return GoogleCredentials.fromStream(inputStream)
        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"))
}

fun callCloudVision(bitmap: Bitmap, context: Context, onTextRecognized: (String) -> Unit) {
    try {
        // Get credentials from the assets (service account)
        val credentials = getVisionCredentialsFromAssets(context)

        // Initialize the Vision API client with credentials
        val imageAnnotatorSettings = ImageAnnotatorSettings.newBuilder()
            .setCredentialsProvider { credentials }
            .build()

        val imageAnnotatorClient = ImageAnnotatorClient.create(imageAnnotatorSettings)

        // Convert bitmap to base64
        val base64Image = encodeBitmapToBase64(bitmap)

        // Create the image request
        val img = Image.newBuilder().setContent(ByteString.copyFrom(Base64.decode(base64Image, Base64.DEFAULT))).build()
        val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).setMaxResults(5).build()
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feature)
            .setImage(img)
            .build()

        // Call the Vision API
        val response = imageAnnotatorClient.batchAnnotateImages(Collections.singletonList(request))

        // Handle the API response
        handleVisionResponse(response, context, onTextRecognized)

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
